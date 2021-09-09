package io.quarkus.ts.security.keycloak.authz;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;

public abstract class BaseAuthzSecurityIT {

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
        getApp().given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + NORMAL_USER));
    }

    @Test
    public void normalUserUserResourceIssuer() {
        getApp().given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get("/user/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("user token issued by " + getKeycloak().getHost()));
    }

    @Test
    public void normalUserAdminResource() {
        getApp().given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void adminUserUserResource() {
        getApp().given()
                .when()
                .auth().oauth2(getToken(ADMIN_USER, ADMIN_USER))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + ADMIN_USER));
    }

    @Test
    public void adminUserAdminResource() {
        getApp().given()
                .when()
                .auth().oauth2(getToken(ADMIN_USER, ADMIN_USER))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, admin " + ADMIN_USER));
    }

    @Test
    public void adminUserAdminResourceIssuer() {
        getApp().given()
                .when()
                .auth().oauth2(getToken(ADMIN_USER, ADMIN_USER))
                .get("/admin/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("admin token issued by " + getKeycloak().getHost()));
    }

    @Test
    public void noUserUserResource() {
        getApp().given()
                .when()
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void noUserAdminResource() {
        getApp().given()
                .when()
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();

    private String getToken(String userName, String password) {
        return authzClient.obtainAccessToken(userName, password).getToken();
    }
}
