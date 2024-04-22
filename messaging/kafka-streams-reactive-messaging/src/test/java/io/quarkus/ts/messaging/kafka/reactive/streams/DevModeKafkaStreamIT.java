package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-1026")
@Tag("QUARKUS-959")
@QuarkusScenario
@DisabledOnRHBQandWindows(reason = "QUARKUS-3434")
public class DevModeKafkaStreamIT extends BaseKafkaStreamTest {

    /**
     * Kafka must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.devservices.enabled", Boolean.TRUE.toString());

    @Override
    protected String getAppUrl() {
        return app.getURI().withScheme("http").toString();
    }

    @Test
    public void kafkaContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/vectorized/redpanda");
    }
}
