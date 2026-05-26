package io.quarkus.ts.lifecycle;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("QUARKUS-7610")
public class JacksonGracefulShutdownIT {

    private static final String TIMED_OUT_MESSAGE = "Timed out waiting for graceful shutdown, shutting down anyway.";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("jackson.shutdown.test.enabled", "true")
            .withProperty("quarkus.shutdown.timeout", "5s");

    @Test
    public void shouldShutdownWithoutTimeout() {
        await().atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .until(() -> app.getLogs().stream().anyMatch(line -> line.contains("stopped in")));

        app.logs().assertContains(JacksonShutdownValidatorBean.SHUTDOWN_INITIATED);
        app.logs().assertDoesNotContain(TIMED_OUT_MESSAGE);
    }
}
