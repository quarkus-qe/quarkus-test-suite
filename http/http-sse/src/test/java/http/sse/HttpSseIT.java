package http.sse;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
@Tag("QQE-257")
// validate issue from https://github.com/quarkusio/quarkus/issues/36402
// this test should only fail on native
public class HttpSseIT {
    private static final String SSE_ERROR_MESSAGE = "java.lang.ClassNotFoundException: Provider for jakarta.ws.rs.sse.SseEventSource.Builder cannot be found";

    @Test
    public void testWorkingSse() {
        String response = given().when().get("/sse").thenReturn().body().asString();

        assertFalse(response.contains(SSE_ERROR_MESSAGE),
                "SSE failed, https://github.com/quarkusio/quarkus/issues/36402 not fixed");
        assertTrue(response.contains("event: test234 test"), "SSE failed, unknown bug. Response: " + response);
    }
}
