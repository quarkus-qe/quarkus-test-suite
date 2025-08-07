package io.quarkus.ts.security.keycloak;

import static io.restassured.RestAssured.given;
import static java.util.Base64.getEncoder;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.Header;
import io.restassured.http.Headers;

public abstract class AbstractMultiMechanismAuthenticationIT {

    static final String KC_NORMAL_USER = "test-normal-user";
    static final String KC_ADMIN_USER = "test-admin-user";
    static final String BASIC_NORMAL_USER = "user";
    static final String BASIC_ADMIN_USER = "admin";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
            .withProperties("multi-mechanism.properties")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void testAdminBasicAndOidcTogetherOnAdminResource() {
        given().headers(prepareHeader(KC_ADMIN_USER, BASIC_ADMIN_USER))
                .when().get("/admin")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testAdminBasicAndOidcTogetherOnUserResource() {
        given().headers(prepareHeader(KC_ADMIN_USER, BASIC_ADMIN_USER))
                .when().get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testUserBasicAndOidcTogetherOnUserResource() {
        given().headers(prepareHeader(KC_NORMAL_USER, BASIC_NORMAL_USER))
                .when().get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testUserBasicAndOidcTogetherOnAdminResource() {
        given().headers(prepareHeader(KC_NORMAL_USER, BASIC_NORMAL_USER))
                .when().get("/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testAdminOnlyOneMechanism() {
        given().headers(prepareHeader(null, BASIC_ADMIN_USER))
                .when().get("/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        given().headers(prepareHeader(KC_ADMIN_USER, null))
                .when().get("/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testUserOnlyOneMechanism() {
        given().headers(prepareHeader(null, BASIC_NORMAL_USER))
                .when().get("/user")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

        given().headers(prepareHeader(KC_NORMAL_USER, null))
                .when().get("/user")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private static Headers prepareHeader(String oidcUser, String basicUser) {
        List<Header> headers = new ArrayList<>();
        if (oidcUser != null) {
            headers.add(new Header("Authorization", "Bearer " + createToken(oidcUser, oidcUser)));
        }
        if (basicUser != null) {
            headers.add(new Header("Authorization", "Basic " + encodeBasicAuthentication(basicUser, basicUser)));
        }
        return new Headers(headers);
    }

    private static String createToken(String username, String password) {
        return keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(username, password).getToken();
    }

    private static String encodeBasicAuthentication(String username, String password) {
        String credentialString = username + ":" + password;
        return getEncoder().encodeToString(credentialString.getBytes());
    }
}
