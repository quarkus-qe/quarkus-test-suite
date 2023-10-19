package io.quarkus.ts.security.webauthn;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractWebAuthnTest {

    protected abstract RestService getApp();

    private static final String REGISTER_URL = "/q/webauthn/register";
    private static final String REGISTER_CALLBACK_URL = "/q/webauthn/callback";
    private static final String LOGIN_URL = "/q/webauthn/login";
    private static final String LOGOUT_URL = "/q/webauthn/logout";

    private static final String PUBLIC_API_URL = "/api/public";
    private static final String PUBLIC_ME_API_URL = "/api/public/me";
    private static final String USER_API_URL = "/api/users/me";
    private static final String ADMIN_API_URL = "/api/admin";

    private static final String USERNAME = "Roosvelt";

    private static Filter cookieFilter;

    enum User {
        USER,
        ADMIN;
    }

    @BeforeAll
    public static void setup() {
        cookieFilter = new CookieFilter();

    }

    @Test
    @Order(1)
    public void checkLogoutInitial() {
        verifyLoggedOut(cookieFilter);
    }

    @Test
    @Order(2)
    public void checkAdminAPIWithoutUser() {
        given()
                .redirects().follow(false)
                .get(ADMIN_API_URL)
                .then()
                .statusCode(302);
    }

    @Test
    @Order(3)
    public void checkMeAPIWithoutUser() {
        given()
                .redirects().follow(false)
                .get(USER_API_URL)
                .then()
                .statusCode(302);
    }

    @Test
    @Order(4)
    public void checkPublicAPI() {
        given()
                .get(PUBLIC_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is("public"));
    }

    @Test
    @Order(5)
    public void testRegisterWebAuthnUser() {
        MyWebAuthnHardware myWebAuthnHardware = new MyWebAuthnHardware();
        String challenge = getChallenge(USERNAME, cookieFilter);
        JsonObject registrationJson = myWebAuthnHardware.makeRegistrationJson(challenge);
        invokeCallback(registrationJson, cookieFilter);
        verifyLoggedIn(cookieFilter, USERNAME, User.USER);
        invokeUserLogout();

    }

    @Test
    @Order(6)
    public void testRegisterSameUserName() {
        MyWebAuthnHardware myWebAuthnHardware = new MyWebAuthnHardware();
        String challenge = getChallenge(USERNAME, cookieFilter);
        JsonObject registrationJson = myWebAuthnHardware.makeRegistrationJson(challenge);
        invokeCallback(registrationJson, cookieFilter);
        verifyLoggedIn(cookieFilter, USERNAME, User.USER);

    }

    @Test
    @Order(7)
    public void testFailLoginWithFakeRegisterUser() {
        invokeUserLogout();
        String newUserName = "Kipchoge";
        ExtractableResponse<Response> response = given().filter(cookieFilter)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + newUserName + "\"}")
                .post(REGISTER_URL)
                .then()
                .statusCode(is(200)).extract();

        given().filter(cookieFilter)
                .get(PUBLIC_ME_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is("<not logged in>"));

        given().filter(cookieFilter)
                .when()
                .get(ADMIN_API_URL)
                .then()
                .statusCode(404);
    }

    public static void invokeCallback(JsonObject registration, Filter cookieFilter) {
        RestAssured
                .given().body(registration.encode()).filter(cookieFilter).contentType(ContentType.JSON).log()
                .ifValidationFails().post(REGISTER_CALLBACK_URL, new Object[0]).then().statusCode(204).log()
                .ifValidationFails().cookie("_quarkus_webauthn_challenge", Matchers.is(""))
                .cookie("_quarkus_webauthn_username", Matchers.is("")).cookie("quarkus-credential", Matchers.notNullValue());

    }

    public static String getChallenge(String userName, Filter cookieFilter) {
        JsonObject registerJson = new JsonObject().put("name", userName);
        ExtractableResponse<Response> response = given()
                .body(registerJson.encode())
                .contentType(ContentType.JSON)
                .filter(cookieFilter)
                .post(REGISTER_URL)
                .then()
                .statusCode(200)
                .cookie("_quarkus_webauthn_challenge", Matchers.notNullValue())
                .cookie("_quarkus_webauthn_username", Matchers.notNullValue()).extract();
        JsonObject responseJson = new JsonObject(response.asString());
        String challenge = responseJson.getString("challenge");
        Assertions.assertNotNull(challenge);
        return challenge;
    }

    private void verifyLoggedIn(Filter cookieFilter, String userName, User user) {

        // public API still good
        given().filter(cookieFilter)
                .get(PUBLIC_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is("public"));
        // public API user name
        given().filter(cookieFilter)
                .get(PUBLIC_ME_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is(userName));

        // user API accessible
        given().filter(cookieFilter)
                .get(USER_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is(userName));

        //admin API
        if (user == User.ADMIN) {
            RestAssured.given().filter(cookieFilter)
                    .when()
                    .get(ADMIN_API_URL)
                    .then()
                    .statusCode(200)
                    .body(Matchers.is("admin"));
        } else {
            RestAssured.given().filter(cookieFilter)
                    .when()
                    .get(ADMIN_API_URL)
                    .then()
                    .statusCode(403);
        }

    }

    private void verifyLoggedOut(Filter cookieFilter) {
        // public API still good
        given().filter(cookieFilter)
                .get(PUBLIC_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is("public"));
        // public API user name
        given().filter(cookieFilter)
                .get(PUBLIC_ME_API_URL)
                .then()
                .statusCode(200)
                .body(Matchers.is("<not logged in>"));

        // user API not accessible
        given()
                .filter(cookieFilter)
                .redirects().follow(false)
                .get(USER_API_URL)
                .then()
                .statusCode(302)
                .header("Location", Matchers.matchesRegex(getApp().getHost() + "(:\\d+)?/login.html"));

    }

    public void invokeUserLogout() {
        given()
                .filter(cookieFilter)
                .redirects()
                .follow(false)
                .get(LOGOUT_URL)
                .then()
                .statusCode(302)
                .cookie("quarkus-credential", Matchers.is(""));
    }
}
