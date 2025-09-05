package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    static RestService appHealthEnabled = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-name", "Default Tenant")
            .withProperties(() -> keycloak.getTlsProperties());

    @QuarkusApplication
    static RestService appHealthDisabled = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @QuarkusApplication(properties = "health-enabled.properties")
    static RestService appMultiTenant = new RestService()
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
    static RestService appAllDisabled = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            // Default tenant - Disabled
            .withProperty("quarkus.oidc.tenant-enabled", "false")
            .withProperty("quarkus.oidc.client-name", "Default Disabled")
            // Tenant1 - Also disabled
            .withProperty("quarkus.oidc.tenant1.tenant-enabled", "false")
            .withProperty("quarkus.oidc.tenant1.client-name", "Tenant1 Disabled")
            .withProperties(() -> keycloak.getTlsProperties());

    @QuarkusApplication(properties = "health-management.properties")
    static RestService appManagement = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-name", "Management Tenant")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void testOidcHealthCheckIsDisabledByDefault() {
        JsonObject health = getHealthResponse(appHealthDisabled, 200);
        assertNull(findOidcHealthCheck(health),
                "OIDC Health Check should NOT be present when disabled");
    }

    @Test
    public void testOidcHealthCheckIsEnabled() {
        JsonObject health = getHealthResponse(appHealthEnabled, 200);
        JsonObject oidcCheck = findOidcHealthCheck(health);

        assertNotNull(oidcCheck, "OIDC Health Check should be present");
        assertEquals("UP", oidcCheck.getString("status"));
        assertEquals("OK", oidcCheck.getJsonObject("data").getString("Default Tenant"));
    }

    @Test
    public void testMultipleTenantsMixedStatuses() {
        JsonObject health = getHealthResponse(appMultiTenant, 200);
        JsonObject oidcCheck = findOidcHealthCheck(health);

        assertNotNull(oidcCheck);
        assertEquals("UP", oidcCheck.getString("status"),
                "Overall status should be UP when at least one tenant is OK");

        JsonObject data = oidcCheck.getJsonObject("data");
        assertNotNull(data);
        assertEquals("OK", data.getString("Default OK"));
        assertEquals("Disabled", data.getString("Tenant1 Disabled"));
        assertEquals("Unknown", data.getString("Tenant2 Unknown"));
        assertEquals(3, data.size(), "Should report status for all 3 tenants");
    }

    @Test
    public void testAllTenantsDisabled() {
        JsonObject health = getHealthResponse(appAllDisabled, 503);
        JsonObject oidcCheck = findOidcHealthCheck(health);

        assertNotNull(oidcCheck);
        assertEquals("DOWN", oidcCheck.getString("status"),
                "Overall status should be DOWN when no tenants are OK");

        JsonObject data = oidcCheck.getJsonObject("data");
        assertEquals("Disabled", data.getString("Default Disabled"));
        assertEquals("Disabled", data.getString("Tenant1 Disabled"));
        assertEquals(2, data.size());
    }

    @Test
    public void testManagementInterface() {
        Response response = appManagement.given()
                .get(HEALTH_READY_PATH);
        response.then().statusCode(404);

        Response mgmtResponse = appManagement.given()
                .port(9000)
                .basePath("/q/health")
                .get("/ready");

        mgmtResponse.then()
                .statusCode(200)
                .body("status", is("UP"));

        JsonObject mgmtHealth = new JsonObject(mgmtResponse.asString());
        JsonObject mgmtCheck = findOidcHealthCheck(mgmtHealth);

        assertNotNull(mgmtCheck, "OIDC Health Check should be available on management interface");
        assertEquals("OK", mgmtCheck.getJsonObject("data").getString("Management Tenant"));
    }

    private JsonObject getHealthResponse(RestService app, int expectedStatus) {
        Response response = app.given().get(HEALTH_READY_PATH);
        response.then().statusCode(expectedStatus);
        return new JsonObject(response.asString());
    }

    private JsonObject findOidcHealthCheck(JsonObject health) {
        JsonArray checks = health.getJsonArray("checks");
        if (checks == null) {
            return null;
        }

        return checks.stream()
                .map(JsonObject.class::cast)
                .filter(check -> OIDC_HEALTH_CHECK_NAME.equals(check.getString("name")))
                .findFirst()
                .orElse(null);
    }
}
