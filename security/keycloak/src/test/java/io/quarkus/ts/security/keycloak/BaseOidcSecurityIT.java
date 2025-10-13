package io.quarkus.ts.security.keycloak;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public abstract class BaseOidcSecurityIT {

    static final String NORMAL_USER = "test-normal-user";
    static final String ADMIN_USER = "test-admin-user";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    private AuthzClient authzClient;

    @BeforeEach
    public void setup() {
        authzClient = getKeycloak().createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT);
    }

    @Test
    public void normalUserUserResource() {
        given()
                .when()
                .auth().oauth2(getUserAccessToken())
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + NORMAL_USER));
    }

    @Test
    public void normalUserUserResourceIssuer() {
        given()
                .when()
                .auth().oauth2(getUserAccessToken())
                .get("/user/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("user token issued by " +
                        getKeycloak().getURI(Protocol.HTTPS).getRestAssuredStyleUri()));
    }

    @Test
    public void normalUserAdminResource() {
        given()
                .when()
                .auth().oauth2(getUserAccessToken())
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void adminUserUserResource() {
        given()
                .when()
                .auth().oauth2(getAdminAccessToken())
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + ADMIN_USER));
    }

    @Test
    public void adminUserAdminResource() {
        given()
                .when()
                .auth().oauth2(getAdminAccessToken())
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, admin " + ADMIN_USER));
    }

    @Test
    public void adminUserAdminResourceIssuer() {
        given()
                .when()
                .auth().oauth2(getAdminAccessToken())
                .get("/admin/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("admin token issued by " +
                        getKeycloak().getURI(Protocol.HTTPS).getRestAssuredStyleUri()));
    }

    @Test
    public void noUserUserResource() {
        given()
                .when()
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void noUserAdminResource() {
        given()
                .when()
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void tokenExpirationUserResource() {
        String token = getUserAccessToken();
        // According to property `quarkus.oidc.token.lifespan-grace`
        // and the property `accessTokenLifespan` in the keycloak configuration,
        // we need to wait more than 5 seconds for the token expiration.
        await().atMost(1, TimeUnit.MINUTES).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .auth().oauth2(token)
                    .get("/user")
                    .then()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED);
        });
    }

    @Test
    @Tag("QUARKUS-5653")
    public void tokenProgrammaticRevocationFlow() {
        AccessTokenResponse tokenResponse = getTokenCreationResponse(NORMAL_USER, NORMAL_USER);
        String initialRefreshToken = tokenResponse.getRefreshToken();
        String initialAccessToken = tokenResponse.getToken();

        // Ensure a refresh token is present
        assertNotNull(initialRefreshToken, "Refresh token should not be null");

        // Ensure the initial access token is active
        given()
                .auth().oauth2(initialAccessToken)
                .body(initialAccessToken)
                .post("/oidc-provider-client/token/is-active")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("active"));

        // Assert that access token can be used to access userinfo
        given()
                .auth().oauth2(initialAccessToken)
                .body(initialAccessToken)
                .post("/oidc-provider-client/username/from-token")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(NORMAL_USER));

        // Revoke access and refresh tokens
        given()
                .auth().oauth2(initialAccessToken)
                .body(initialRefreshToken)
                .post("/oidc-provider-client/token/revoke")
                .then()
                .statusCode(HttpStatus.SC_OK);

        String newAccessToken = getTokenCreationResponse(NORMAL_USER, NORMAL_USER).getToken();

        // Ensure the initial access token is inactive after revocation
        given()
                .auth().oauth2(newAccessToken)
                .body(initialAccessToken)
                .post("/oidc-provider-client/token/is-active")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("inactive"));

        // Access to the protected resource with the old access token should fail
        given()
                .auth().oauth2(initialAccessToken)
                .body(initialAccessToken)
                .post("/oidc-provider-client/username/from-token")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        // Attempt to use the old refresh token to get a new access token
        Response refreshGrantResponse = given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.URLENC)
                .formParams(Map.of(
                        "grant_type", "refresh_token",
                        "client_id", CLIENT_ID_DEFAULT,
                        "client_secret", CLIENT_SECRET_DEFAULT,
                        "refresh_token", initialRefreshToken))
                .post(getKeycloakRealmUrl() + "/protocol/openid-connect/token");

        // Assert that the old refresh token is no longer valid
        assertEquals(HttpStatus.SC_BAD_REQUEST, refreshGrantResponse.statusCode());
        assertEquals("invalid_grant", refreshGrantResponse.jsonPath().getString("error"));
    }

    @Test
    public void noMetadataUnlessEnabled() {
        given()
                .when()
                .auth().oauth2(getUserAccessToken())
                .get(".well-known/oauth-protected-resource")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        Response response = given()
                .when()
                .get("/user");
        response.then().statusCode(HttpStatus.SC_UNAUTHORIZED);
        response.headers().asList().forEach(header -> {
            assertFalse(header.getValue().contains("resource_metadata"),
                    "Header contains info about a private resource: " + header);
            assertFalse(header.getValue().contains(".well-known"),
                    "Header may contain info about a private resource: " + header);
        });
    }

    protected abstract KeycloakService getKeycloak();

    private AccessTokenResponse getTokenCreationResponse(String username, String password) {
        return authzClient.obtainAccessToken(username, password);
    }

    private String getUserAccessToken() {
        return getTokenCreationResponse(NORMAL_USER, NORMAL_USER).getToken();
    }

    protected String getAdminAccessToken() {
        return getTokenCreationResponse(ADMIN_USER, ADMIN_USER).getToken();
    }

    protected String getKeycloakRealmUrl() {
        return getKeycloak().getRealmUrl();
    }
}
