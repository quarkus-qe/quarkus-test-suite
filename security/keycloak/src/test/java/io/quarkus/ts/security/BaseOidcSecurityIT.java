package io.quarkus.ts.security;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;

import io.quarkus.test.bootstrap.KeycloakService;

public abstract class BaseOidcSecurityIT {

    static final String NORMAL_USER = "test-normal-user";
    static final String ADMIN_USER = "test-admin-user";
    static final String REALM_DEFAULT = "test-realm";
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
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + NORMAL_USER));
    }

    @Test
    public void normalUserUserResourceIssuer() {
        given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get("/user/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("user token issued by " + getKeycloak().getHost()));
    }

    @Test
    public void normalUserAdminResource() {
        given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void adminUserUserResource() {
        given()
                .when()
                .auth().oauth2(getToken(ADMIN_USER, ADMIN_USER))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + ADMIN_USER));
    }

    @Test
    public void adminUserAdminResource() {
        given()
                .when()
                .auth().oauth2(getToken(ADMIN_USER, ADMIN_USER))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, admin " + ADMIN_USER));
    }

    @Test
    public void adminUserAdminResourceIssuer() {
        given()
                .when()
                .auth().oauth2(getToken(ADMIN_USER, ADMIN_USER))
                .get("/admin/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("admin token issued by " + getKeycloak().getHost()));
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
        String token = getToken(NORMAL_USER, NORMAL_USER);
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

    protected abstract KeycloakService getKeycloak();

    private String getToken(String userName, String password) {
        return authzClient.obtainAccessToken(userName, password).getToken();
    }
}
