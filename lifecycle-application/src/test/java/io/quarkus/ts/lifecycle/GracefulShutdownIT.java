package io.quarkus.ts.lifecycle;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("https://github.com/quarkusio/quarkus/issues/49733")
public class GracefulShutdownIT {

    private static final String TASK_COMPLETED = "GracefulShutdownValidatorTask completed";
    private static final String TASK_INTERRUPTED = "GracefulShutdownValidatorTask interrupted";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("graceful.shutdown.test.enabled", "true")
            .withProperty("quarkus.thread-pool.shutdown-interrupt", "10s")
            .withProperty("quarkus.thread-pool.shutdown-check-interval", "1s")
            .withProperty("quarkus.log.category.\"io.quarkus.thread-pool\".level", "DEBUG");

    @Test
    public void shouldCompleteTaskBeforeShutdownInterruptTimeout() {
        await().atMost(12, SECONDS)
                .pollInterval(1, SECONDS)
                .until(() -> {
                    String logs = String.join("\n", app.getLogs());
                    return logs.contains("stopped in");
                });

        String logs = String.join("\n", app.getLogs());

        assertTrue(logs.contains(TASK_COMPLETED),
                "Task should complete before 10s timeout. Bug present if interrupted prematurely.");

        assertFalse(logs.contains(TASK_INTERRUPTED),
                "Task should not be interrupted. Bug present if timing calculation is incorrect.");
    }
}