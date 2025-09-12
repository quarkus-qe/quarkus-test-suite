package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class AbstractMultiTenantHealthCheckIT {
    private static final String OIDC_HEALTH_CHECK_NAME = "OIDC Provider Health Check";
    private static final String HEALTH_READY_PATH = "/q/health/ready";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication(properties = "health-enabled.properties")
    static RestService appmultitenant = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            // Default tenant - OK
            .withProperty("quarkus.oidc.client-name", "Default OK")
            // Tenant1 - Disabled
            .withProperty("quarkus.oidc.tenant1.tenant-enabled", "false")
            .withProperty("quarkus.oidc.tenant1.client-name", "Tenant1 Disabled")
            // Tenant2 - Unknown (requires introspection-path to start)
            .withProperty("quarkus.oidc.tenant2.auth-server-url", "http://localhost:8888")
            .withProperty("quarkus.oidc.tenant2.discovery-enabled", "false")
            .withProperty("quarkus.oidc.tenant2.introspection-path", "/introspect")
            .withProperty("quarkus.oidc.tenant2.client-name", "Tenant2 Unknown")
            .withProperties(() -> keycloak.getTlsProperties());

    @QuarkusApplication(properties = "health-enabled.properties")
    static RestService appalldisabled = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            // Default tenant - Disabled
            .withProperty("quarkus.oidc.tenant-enabled", "false")
            .withProperty("quarkus.oidc.client-name", "Default Disabled")
            // Tenant1 - Also disabled
            .withProperty("quarkus.oidc.tenant1.tenant-enabled", "false")
            .withProperty("quarkus.oidc.tenant1.client-name", "Tenant1 Disabled")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void testMultipleTenantsMixedStatuses() {
        JsonObject health = getHealthResponse(appmultitenant, HttpStatus.SC_OK);
        Optional<JsonObject> oidcCheck = findOidcHealthCheck(health);
        assertTrue(oidcCheck.isPresent(), "OIDC Health Check should be present");
        JsonObject oidcCheckJson = oidcCheck.get();
        assertEquals("UP", oidcCheckJson.getString("status"),
                "Overall status should be UP when at least one tenant is OK");

        JsonObject data = oidcCheckJson.getJsonObject("data");
        assertNotNull(data);
        assertEquals("OK", data.getString("Default OK"));
        assertEquals("Disabled", data.getString("Tenant1 Disabled"));
        assertEquals("Unknown", data.getString("Tenant2 Unknown"));
        assertEquals(3, data.size(), "Should report status for all 3 tenants");
    }

    @Test
    public void testAllTenantsDisabled() {
        JsonObject health = getHealthResponse(appalldisabled, HttpStatus.SC_SERVICE_UNAVAILABLE);
        Optional<JsonObject> oidcCheck = findOidcHealthCheck(health);

        assertTrue(oidcCheck.isPresent(), "OIDC Health Check should be present");
        JsonObject oidcCheckJson = oidcCheck.get();
        assertEquals("DOWN", oidcCheckJson.getString("status"),
                "Overall status should be DOWN when no tenants are OK");

        JsonObject data = oidcCheckJson.getJsonObject("data");
        assertEquals("Disabled", data.getString("Default Disabled"));
        assertEquals("Disabled", data.getString("Tenant1 Disabled"));
        assertEquals(2, data.size());
    }

    private JsonObject getHealthResponse(RestService app, int expectedStatus) {
        Response response = app.given().get(HEALTH_READY_PATH);
        response.then().statusCode(expectedStatus);
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
