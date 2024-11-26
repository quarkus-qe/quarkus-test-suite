package io.quarkus.ts.messaging.kafka.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.messaging.kafka.processor.decorator.HeaderDecorator;

@Tag("QUARKUS-5178")
@QuarkusScenario
public class DevModeKafkaProcessorIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().setAutoStart(false);

    /**
     * The QUARKUS-5178 was caused only in dev mode and not all the time.
     * The selected 5 runs start stops should be enough to detect the original issue
     */
    @Test
    public void quarkusShouldStartWithoutFailTest() {
        // As QUARKUS-5178 not occurring all the time so occasionally we need to check the dev mode multiple time
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> app.start(),
                    "The QUARKUS-5178 is probably not fixed");
            app.logs().assertDoesNotContain("java.lang.ClassNotFoundException: " + HeaderDecorator.class.getName());
            app.stop();
        }
    }
}
