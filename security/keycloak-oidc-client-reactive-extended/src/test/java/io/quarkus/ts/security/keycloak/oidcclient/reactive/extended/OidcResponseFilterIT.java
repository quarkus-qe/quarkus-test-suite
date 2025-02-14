package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.Response;

@QuarkusScenario
public class OidcResponseFilterIT extends BaseOidcIT {
    private static final Logger LOG = Logger.getLogger(OidcResponseFilterIT.class);
    static AccessTokenResponse tokenResponse;

    protected AccessTokenResponse createTokenWithoutProfileScope() {
        return keycloak
                .createAuthzClient("test-application-client-partial", "test-application-client-partial-secret")
                .obtainAccessToken(USER, USER);
    }

    @BeforeEach
    public void setUp() {
        tokenResponse = BaseOidcIT.keycloak
                .createAuthzClient(BaseOidcIT.CLIENT_ID_DEFAULT, BaseOidcIT.CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(BaseOidcIT.USER, BaseOidcIT.USER);
    }

    /**
     * Client-side test scenario
     * Uses quarkus-oidc-client extension to test the refresh token flow and verify that the
     * TokenResponseFilter is triggered.
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

        // Verify that the TokenResponseFilter was triggered and logs contain the expected message
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
     * using an invalid token and verify the error from response filter
     */
    @Test
    public void testInvalidRefreshToken() {
        Response response = given()
                .queryParam("refreshToken", "invalid_refresh_token")
                .when()
                .get("/token/refresh")
                .then()
                .statusCode(500)
                .extract()
                .response();
        String invalidRefreshTokenResponse = response.asString();
        Assertions.assertTrue(invalidRefreshTokenResponse.contains("Invalid token"));
        app.logs().assertDoesNotContain("Tokens have been refreshed");
        app.logs().assertContains("Invalid refresh token");
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
        Assertions.assertTrue(
                userinfoFilterLogs.matches("(?s).*Userinfo sub:\\s+\\S+.*"),
                "Log should contain `Userinfo sub:` followed by a non-empty string.");
        app.logs().assertContains("Userinfo 'sub':");
        app.logs().assertContains("Userinfo 'preferred_username': test-user");
    }

    /**
     * Negative server-side test scenario
     * Attempt to retrieve user info with invalid token
     */
    @Test
    public void testUserInfoWithInvalidToken() {
        // lets modify the token
        String invalidToken = tokenResponse.getToken() + "hehe";

        Response response_invalidToken = given()
                .auth().oauth2(invalidToken)
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(401)
                .extract()
                .response();
    }

    /**
     * Negative server-side test scenario for userinfo
     * Using a valid token but from a client that doesn't include the "profile" scope by default,
     * so we expect "preferred_username" to be missing in userinfo.
     */
    @Test
    public void testUserInfoWithPartialClaims() {
        AccessTokenResponse partialToken = createTokenWithoutProfileScope();

        Response resp = given()
                .auth().oauth2(partialToken.getToken())
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String responseBody = resp.asString();
        LOG.infof("Userinfo partial response: %s", responseBody);

        Assertions.assertFalse(responseBody.contains("preferred_username"),
                "userinfo should not contain preferred_username claim because 'profile' scope is missing");

        Response filterLogsResp = given()
                .when()
                .get("/filter-messages/userinfo")
                .then()
                .statusCode(200)
                .extract().response();
        String userinfoFilterLogs = filterLogsResp.asString();
        Assertions.assertTrue(userinfoFilterLogs.contains("'preferred_username' claim not found in userinfo"),
                "Userinfo filter log must indicate missing 'preferred_username' claim when using partial scopes");
    }

    /**
     * JWKS Server-side test scenario
     * a secured enpoint is accesed
     * the OIDC authentication mechanism in Quarkus (used quarkus-oidc extension)
     * and automatically (This triggers the server side) retrieves the JWKS.
     */
    @Test
    public void jwksFilterTriggered() {
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
    public void jwksInvalidTokenNotTrigger() {
        given()
                .auth().oauth2("invalid.token")
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(401);

    }
}
