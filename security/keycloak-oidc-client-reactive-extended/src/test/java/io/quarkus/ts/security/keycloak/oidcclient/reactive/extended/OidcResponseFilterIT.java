package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.Response;

@QuarkusScenario
public class OidcResponseFilterIT extends BaseOidcIT {
    private static final Logger LOG = Logger.getLogger(OidcResponseFilterIT.class);
    static AccessTokenResponse tokenResponse;

    @BeforeAll
    public static void setUp() {
        tokenResponse = BaseOidcIT.keycloak
                .createAuthzClient(BaseOidcIT.CLIENT_ID_DEFAULT, BaseOidcIT.CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(BaseOidcIT.USER, BaseOidcIT.USER);
    }

    /**
     * Client-side test scenario
     * Uses quarkus-oidc-client extension
     */
    @Test
    public void testRefreshTokenFlowTriggerResponseFilter() {

        String accessToken = tokenResponse.getToken();
        String refreshToken = tokenResponse.getRefreshToken();

        LOG.infof("Initial Access Token acquired: %s", accessToken);
        LOG.infof("Initial Refresh Token acquired: %s", refreshToken);

        Response refreshResponse = given()
                .queryParam("refreshToken", refreshToken)
                .when()
                .get("/token/refresh")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .extract().response();

        String refreshedTokens = refreshResponse.asString();

        LOG.infof("Refreshed tokens response: %s", refreshedTokens);

        String[] tokens = refreshedTokens.split(" ");
        Assertions.assertEquals(2, tokens.length, "There should be 2 tokens in the response");
        Assertions.assertNotEquals(accessToken, tokens[0], "Access token should be refreshed.");
        Assertions.assertNotEquals(refreshToken, tokens[1], "Refresh token should be refreshed.");

        // Verify that the TokenResponseFilter was triggered

        Response logResponse = given()
                .when()
                .get("/filter-messages/token")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .extract().response();
        String tokenFilterLogs = logResponse.asString();
        LOG.infof("Token Filter Logs: %s", tokenFilterLogs);
        Assertions.assertTrue(tokenFilterLogs.contains("Tokens have been refreshed"),
                "Token filter log must indicate tokens have been refreshed");
        app.logs().assertContains("Tokens have been refreshed");
    }

    /**
     * Negative client-side test scenario
     * using an invalid token
     */
    @Test
    public void testInvalidRefreshToken() {
        given()
                .queryParam("refreshToken", "invalid_refresh_token")
                .when()
                .get("/token/refresh")
                .then()
                .statusCode(500);

    }

    /**
     * Server-side test scenario
     * used quarkus-oidc extension
     * UserInfo is injected
     */
    @Test
    public void userInfoFilterIsTriggered() {
        String accessToken = tokenResponse.getToken();
        LOG.infof("Access Token userInfoFilterIsTriggered : %s", accessToken);

        Response resp = given()
                .auth().oauth2(accessToken)
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(200)
                .body("sub", not(emptyString()))
                .body("preferred_username", equalTo("test-user"))
                .extract()
                .response();

        LOG.infof("Userinfo response: %s", resp.asString());

        // Verify that the UserinfoResponseFilter was triggered

        Response logResponse = given()
                .when()
                .get("/filter-messages/userinfo")
                .then()
                .statusCode(200)
                .body(notNullValue())
                .extract().response();
        String userinfoFilterLogs = logResponse.asString();
        Assertions.assertTrue(userinfoFilterLogs.contains("Userinfo sub:"),
                "Userinfo filter log must indicate that sub was logged");

        app.logs().assertContains("Userinfo sub: ", "preferred_username");

    }

    /**
     * Negative server-side test scenario
     * Attempt to retrieve user info without token
     */
    @Test
    public void testUserInfoWithoutToken() {
        given()
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(401);

    }

    /**
     * JWKS Server-side test scenario
     * a secured enpoint is accesed
     * the OIDC authentication mechanism in Quarkus (used quarkus-oidc extension)
     * and automatically (This triggers the server side) retrieves the JWKS.
     */
    @Test
    public void testJwksFilterTriggered() {
        AccessTokenResponse accessToken = keycloak
                .createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(USER, USER);
        Response response = given()
                .auth().oauth2(accessToken.getToken())
                .when()
                .get("/secured/getClaimsFromBeans")
                .then()
                .statusCode(200)
                .extract().response();

        String json = response.asString();
        LOG.infof("Secured response after forcing JWKS: %s", json);

        // Verify that the JWKSResponseFilter was triggered

        Response logResponse = given()
                .when()
                .get("/filter-messages/jwks")
                .then()
                .statusCode(200)
                .extract().response();
        String jwksFilterLogs = logResponse.asString();
        LOG.infof("JWKS Filter Logs: %s", jwksFilterLogs);
        Assertions.assertTrue(jwksFilterLogs.contains("JWKS response intercepted"),
                "JWKS filter log must indicate it was intercepted");
        app.logs().assertContains("JWKS response intercepted");
    }

    /**
     * Negative JWKS test scenario:
     * uses an invalid token to ensure that the JWKSResponseFilter
     * behaves correctly when an unauthorized request is made.
     */
    @Test
    public void jwksFilterIsTriggered() {
        given()
                .auth().oauth2("invalid.token")
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(401);

    }

}
