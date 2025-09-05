package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Tag("https://issues.redhat.com/browse/QUARKUS-6263")
public abstract class AbstractHealthCheckIT {

    private static final String OIDC_HEALTH_CHECK_NAME = "OIDC Provider Health Check";
    private static final String HEALTH_READY_PATH = "/q/health/ready";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication(properties = "health-enabled.properties")
    static RestService apphealthenabled = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-name", "Default Tenant")
            .withProperties(() -> keycloak.getTlsProperties());

    @QuarkusApplication
    static RestService apphealthdisabled = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @QuarkusApplication(properties = "health-management.properties")
    static RestService appmanagement = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-name", "Management Tenant")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void testOidcHealthCheckIsDisabledByDefault() {
        JsonObject health = getHealthResponse(apphealthdisabled);
        Optional<JsonObject> oidcCheck = findOidcHealthCheck(health);
        assertFalse(oidcCheck.isPresent(),
                "OIDC Health Check should NOT be present when disabled");
    }

    @Test
    public void testOidcHealthCheckIsEnabled() {
        JsonObject health = getHealthResponse(apphealthenabled);
        Optional<JsonObject> oidcCheck = findOidcHealthCheck(health);

        assertTrue(oidcCheck.isPresent(), "OIDC Health Check should be present");
        JsonObject oidcCheckJson = oidcCheck.get();
        assertEquals("UP", oidcCheckJson.getString("status"));
        assertEquals("OK", oidcCheckJson.getJsonObject("data").getString("Default Tenant"));
    }

    @Test
    public void testManagementInterface() {
        Response response = appmanagement.given()
                .get(HEALTH_READY_PATH);
        response.then().statusCode(HttpStatus.SC_NOT_FOUND);

        Response mgmtResponse = appmanagement.management()
                .get("/q/health/ready");

        mgmtResponse.then()
                .statusCode(HttpStatus.SC_OK)
                .body("status", is("UP"));

        JsonObject mgmtHealth = new JsonObject(mgmtResponse.asString());
        Optional<JsonObject> mgmtCheck = findOidcHealthCheck(mgmtHealth);
        assertTrue(mgmtCheck.isPresent(), "OIDC Health Check should be available on management interface");
        JsonObject oidcCheckJson = mgmtCheck.get();
        assertEquals("OK", oidcCheckJson.getJsonObject("data").getString("Management Tenant"));
    }

    private JsonObject getHealthResponse(RestService app) {
        Response response = app.given().get(HEALTH_READY_PATH);
        response.then().statusCode(HttpStatus.SC_OK);
        return new JsonObject(response.asString());
    }

    private Optional<JsonObject> findOidcHealthCheck(JsonObject health) {
        JsonArray checks = health.getJsonArray("checks");
        if (checks == null) {
            return Optional.empty();
        }

        return checks.stream()
                .map(JsonObject.class::cast)
                .filter(check -> OIDC_HEALTH_CHECK_NAME.equals(check.getString("name")))
                .findFirst();
    }
}
