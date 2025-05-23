package io.quarkus.ts.security.keycloak;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.security.keycloak.BaseOidcSecurityIT.CLIENT_ID_DEFAULT;
import static io.quarkus.ts.security.keycloak.BaseOidcSecurityIT.CLIENT_SECRET_DEFAULT;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class OidcTokenRevocationIT {

    @KeycloakContainer(command = { "start-dev", "--import-realm" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
            .withProperty("quarkus.oidc.token.refresh-enabled", "true")
            .withProperty("quarkus.oidc.authentication.scopes", "openid offline_access");

    @Test
    public void shouldLogoutAndRevokeTokens() {
        var accessTokenResponse = createToken("test-normal-user", "test-normal-user");

        given()
                .auth().oauth2(accessTokenResponse.getToken())
                .when()
                .get("oidc-prover-client/token/revoke")
                .then()
                .statusCode(200)
                .body(is("Logout, tokens revoked"));
    }

    @Test
    public void tokenRevocationFlow() {
        var accessToken = createToken("test-normal-user", "test-normal-user");
        //        String refreshToken = tokenResponse.get("refresh_token");

        // Assert access with token works before revocation
        given()
                .auth().oauth2(accessToken.getToken())
                .when()
                .get("/oidc-prover-client/username")
                .then()
                .statusCode(200)
                .body(is("test-normal-user"));

        // Revoke tokens
        given()
                .auth().oauth2(accessToken.getToken())
                .when()
                .get("/oidc-prover-client/token/revoke")
                .then()
                .statusCode(200);

        // Access token should be revoked
        given()
                .auth().oauth2(accessToken.getToken())
                .when()
                .get("/oidc-prover-client/username")
                .then()
                .statusCode(401);

        // Refresh token should also be revoked
        //        given()
        //                .contentType("application/x-www-form-urlencoded")
        //                .formParam("grant_type", "refresh_token")
        //                .formParam("client_id", "test-application-client")
        //                .formParam("client_secret", "test-application-client-secret")
        //                .formParam("refresh_token", refreshToken)
        //                .when()
        //                .post(keycloak.getRealmUrl() + "/protocol/openid-connect/token")
        //                .then()
        //                .statusCode(400)
        //                .body("error", equalTo("invalid_grant"));
    }

    @Test
    public void testBackchannelLogoutRevokesTokens() throws Exception {
        // Step 1: Login and obtain access/refresh tokens
        var accessTokenResponse = createToken("test-normal-user", "test-normal-user");

        given()
                .auth().oauth2(accessTokenResponse.getToken())
                .when()
                .get("/oidc-prover-client/username")
                .then()
                .statusCode(200);

        // Trigger backchannel logout
        performBackchannelLogout(accessTokenResponse.getIdToken());

        // Wait for logout to propagate
        Thread.sleep(1000);

        given()
                .auth().oauth2(accessTokenResponse.getToken())
                .when()
                .get("/oidc-prover-client/username")
                .then()
                .statusCode(401);
    }

    private void performBackchannelLogout(String idTokenHint) {
        given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("client_id", "test-client")
                .formParam("client_secret", "test-application-client-secret")
                .formParam("id_token_hint", idTokenHint)
                .when()
                .post(keycloak.getRealmUrl() + "/protocol/openid-connect/logout")
                .then()
                .statusCode(is(200));
    }

    private static AccessTokenResponse createToken(String username, String password) {
        return keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(username, password);
    }
}
