package io.quarkus.ts.security.keycloak.multitenant;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.RestAssured;

public abstract class AbstractStaticMultiTenantUserInfoIT {

    protected static final String USER = "test-user";
    protected static final String CLIENT_ID_DEFAULT = "test-service-client";
    protected static final String CLIENT_SECRET_DEFAULT = "test-service-client-secret";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.tenant-paths", "/user-info/default-tenant-random")
            .withProperty("quarkus.oidc.user-info-path", "${quarkus.oidc.auth-server-url}/protocol/openid-connect/userinfo")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void testDefaultTenant() {
        RestAssured.given().auth().oauth2(createToken(keycloak)).get("/user-info/default-tenant-random").then()
                .statusCode(200)
                .body(Matchers.is("test-user"));
    }

    @Test
    public void testNamedTenant() {
        RestAssured.given().auth().oauth2(createToken(keycloak)).get("/user-info/named-tenant-random").then()
                .statusCode(200)
                .body(Matchers.is("test-user"));
    }

    private static String createToken(KeycloakService keycloak) {
        return keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(USER, USER).getToken();
    }
}
