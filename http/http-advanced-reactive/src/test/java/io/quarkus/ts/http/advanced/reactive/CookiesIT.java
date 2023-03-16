package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.CookiesResource.TEST_COOKIE;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.detailedCookie;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.Cookie;
import io.restassured.response.ValidatableResponse;

@QuarkusScenario
public class CookiesIT {

    @QuarkusApplication(classes = { CookiesResource.class }, properties = "cookies.properties")
    static RestService app = new RestService();

    @Test
    void testLaxSameSiteAttribute() {
        assertSameSiteAttribute("Lax");
    }

    @Test
    void testStrictSameSiteAttribute() {
        assertSameSiteAttribute("Strict");
    }

    @Test
    void testNoneSameSiteAttribute() {
        assertSameSiteAttribute("None");
    }

    @Test
    void testNoSameSiteAttribute() {
        assertSameSiteAttribute("");
    }

    @Test
    void testSameSiteAttributeAddedByVertxHttpExt() {
        String sameSite = "None";
        given()
                .cookie(new Cookie.Builder(TEST_COOKIE, sameSite).setVersion(1).build())
                .get("/cookie/same-site/vertx")
                .then()
                .statusCode(200)
                .body(is(sameSite))
                .cookie("vertx", detailedCookie().sameSite(sameSite).secured(true));
    }

    private static void assertSameSiteAttribute(String sameSite) {
        ValidatableResponse response;

        response = whenCookieParamSameSiteReq(sameSite);
        assertSameSiteInResponse(response, sameSite);

        response = whenFormParamSameSiteReq(sameSite);
        assertSameSiteInResponse(response, sameSite);
    }

    private static ValidatableResponse whenFormParamSameSiteReq(String sameSite) {
        return given()
                .formParam(TEST_COOKIE, CookiesResource.toRawCookie(sameSite))
                .post("/cookie/same-site/form-param")
                .then();
    }

    private static ValidatableResponse whenCookieParamSameSiteReq(String sameSite) {
        var cookie = new Cookie.Builder(TEST_COOKIE, sameSite).setVersion(1).build();
        return given()
                .cookie(cookie)
                .post("/cookie/same-site/cookie-param")
                .then();
    }

    private static void assertSameSiteInResponse(ValidatableResponse response, String sameSite) {
        if (sameSite.isEmpty()) {
            response.statusCode(204)
                    .cookie(TEST_COOKIE, detailedCookie().sameSite((String) null));
        } else {
            sameSite = sameSite.toUpperCase();
            response.statusCode(200)
                    .body(is(sameSite))
                    .cookie(TEST_COOKIE, detailedCookie().sameSite(sameSite));
        }
    }

}
