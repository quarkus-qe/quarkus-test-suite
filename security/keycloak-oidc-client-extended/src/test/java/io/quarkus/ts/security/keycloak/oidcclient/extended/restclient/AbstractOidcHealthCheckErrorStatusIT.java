package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Tag("https://issues.redhat.com/browse/QUARKUS-6263")
@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractOidcHealthCheckErrorStatusIT {

    private static final String OIDC_HEALTH_CHECK_NAME = "OIDC Provider Health Check";
    private static final String HEALTH_READY_PATH = "/q/health/ready";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication(properties = "health-enabled.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-name", "Test Tenant")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    @Order(1)
    public void testHealthCheckOKBeforeStopping() {
        Response response = app.given().get(HEALTH_READY_PATH);
        response.then().statusCode(200);

        JsonObject health = new JsonObject(response.asString());
        Optional<JsonObject> oidcCheck = findOidcHealthCheck(health);
        assertTrue(oidcCheck.isPresent(), "OIDC Health Check should be present");
        JsonObject oidcCheckJson = oidcCheck.get();
        assertEquals("OK", oidcCheckJson.getJsonObject("data").getString("Test Tenant"));
    }

    @Test
    @Order(2)
    public void testErrorStatusWhenProviderStopped() {
        keycloak.stop();

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    Response response = app.given().get(HEALTH_READY_PATH);
                    return response.statusCode() == 503;
                });

        Response response = app.given().get(HEALTH_READY_PATH);
        response.then().statusCode(503);

        JsonObject health = new JsonObject(response.asString());
        Optional<JsonObject> oidcCheck = findOidcHealthCheck(health);
        assertTrue(oidcCheck.isPresent(), "OIDC Health Check should be present even when DOWN");
        JsonObject oidcCheckJson = oidcCheck.get();
        String status = oidcCheckJson.getJsonObject("data").getString("Test Tenant");
        assertTrue(status.startsWith("Error"), "Status should be Error but was: " + status);

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
