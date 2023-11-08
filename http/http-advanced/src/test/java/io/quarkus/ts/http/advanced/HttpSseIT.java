package io.quarkus.ts.http.advanced;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusScenario
@Tag("https://github.com/quarkusio/quarkus/issues/36402")
// this test should only fail on native
public class HttpSseIT {

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");
    private static final String SSE_ERROR_MESSAGE = "java.lang.ClassNotFoundException: Provider for jakarta.ws.rs.sse.SseEventSource.Builder cannot be found";

    @Test
    public void testWorkingSse() {
        String response = app.given().when().get("/api/sse/client").thenReturn().body().asString();

        assertFalse(response.contains(SSE_ERROR_MESSAGE),
                "SSE failed, https://github.com/quarkusio/quarkus/issues/36402 not fixed");
        assertTrue(response.contains("event: test234 test"), "SSE failed, unknown bug. Response: " + response);
    }
}
