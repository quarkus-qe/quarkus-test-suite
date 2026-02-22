package io.quarkus.ts.openshift.security.basic;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class BasicSecurityIT {
    static final String ADMIN_USERNAME = "albert";
    static final String ADMIN_PASSWORD = "E!nst3iN";
    static final String USER_USERNAME = "isaac";
    static final String USER_PASSWORD = "N3wt0N";
    static final String UNKNOWN_USERNAME = "unknown";
    static final String UNKNOWN_PASSWORD = "unknown";
    static final Set<String> USER_ROLE = new HashSet<>(Arrays.asList(ADMIN_USERNAME, USER_USERNAME));
    static final Set<String> ADMIN_ROLE = Collections.singleton(ADMIN_USERNAME);

    @ParameterizedTest(name = "[{index}] user {0}, known: {2}")
    @MethodSource("usernamePasswordCombinations")
    public void permitAll(String username, String password, boolean known) {
        test("/permit-all", username, password)
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello!"));
    }

    @ParameterizedTest(name = "[{index}] user {0}, known: {2}")
    @MethodSource("usernamePasswordCombinations")
    public void denyAll(String username, String password, boolean known) {
        test("/deny-all", username, password)
                .statusCode(known ? HttpStatus.SC_FORBIDDEN : HttpStatus.SC_UNAUTHORIZED);
    }

    @ParameterizedTest(name = "[{index}] user {0}, known: {2}")
    @MethodSource("usernamePasswordCombinations")
    public void everyone(String username, String password, boolean known) {
        test("/everyone", username, password)
                .statusCode(known ? HttpStatus.SC_OK : HttpStatus.SC_UNAUTHORIZED)
                .body(known ? equalTo("Hello, " + username) : anything());
    }

    @ParameterizedTest(name = "[{index}] user {0}, known: {2}")
    @MethodSource("usernamePasswordCombinations")
    public void user(String username, String password, boolean known) {
        boolean ok = known && USER_ROLE.contains(username);
        test("/user", username, password)
                .statusCode(ok ? HttpStatus.SC_OK : HttpStatus.SC_UNAUTHORIZED)
                .body(ok ? equalTo("Hello, user " + username) : anything());
    }

    @ParameterizedTest(name = "[{index}] user {0}, known: {2}")
    @MethodSource("usernamePasswordCombinations")
    public void admin(String username, String password, boolean known) {
        boolean ok = known && ADMIN_ROLE.contains(username);

        int expectedStatus = ok ? HttpStatus.SC_OK : HttpStatus.SC_UNAUTHORIZED;
        if (known && !ADMIN_ROLE.contains(username)) {
            expectedStatus = HttpStatus.SC_FORBIDDEN;
        }

        test("/admin", username, password)
                .statusCode(expectedStatus)
                .body(ok ? equalTo("Hello, admin " + username) : anything());
    }

    @Test
    public void customPolicyOnAnnotations() {
        given()
                .auth().basic("alice", "rabbit")
                .get("/custom/annotated")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user alice"));

        given()
                .auth().basic("bob", "builder")
                .get("/custom/annotated")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        given()
                .auth().basic(USER_USERNAME, USER_PASSWORD)
                .get("/custom/annotated")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        given()
                .auth().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .get("/custom/annotated")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user albert"));
    }

    @Test
    public void customPolicyProperties() {
        given()
                .auth().basic("alice", "rabbit")
                .get("/custom/properties")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        given()
                .auth().basic("bob", "builder")
                .get("/custom/properties")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user bob"));
        given()
                .auth().basic(USER_USERNAME, USER_PASSWORD)
                .get("/custom/properties")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        given()
                .auth().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .get("/custom/properties")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user albert"));
    }

    @Test
    public void customPolicyBoth() {
        given()
                .auth().basic("alice", "rabbit")
                .get("/custom/both")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        given()
                .auth().basic("bob", "builder")
                .get("/custom/both")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        given()
                .auth().basic(USER_USERNAME, USER_PASSWORD)
                .get("/custom/both")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
        given()
                .auth().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .get("/custom/both")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user albert"));
    }

    @Test
    public void invalidBase64ShouldTriggerAuthenticationFailedException() {
        given()
                .header("Authorization", "Basic A")
                .when()
                .get("/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .header("Test-Auth-Exception", "AuthenticationFailedException");
    }

    private ValidatableResponse test(String url, String username, String password) {
        RequestSpecification test = given();
        if (username != null && password != null) {
            test = test.auth().basic(username, password);
        }

        return test.when().get(url).then();
    }

    static Stream<Arguments> usernamePasswordCombinations() {
        return Stream.of(
                Arguments.of(null, null, false),
                Arguments.of(USER_USERNAME, USER_PASSWORD, true),
                Arguments.of(ADMIN_USERNAME, ADMIN_PASSWORD, true),
                Arguments.of(USER_USERNAME, UNKNOWN_PASSWORD, false),
                Arguments.of(ADMIN_USERNAME, UNKNOWN_PASSWORD, false),
                Arguments.of(UNKNOWN_USERNAME, UNKNOWN_PASSWORD, false));
    }
}
