package io.quarkus.ts.messaging.kafka.producer;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class DevModeKafkaSnappyIT extends SnappyCompressionIT {

    @DevModeQuarkusApplication(properties = "devservices.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.devservices.enabled", Boolean.TRUE.toString());

    @Test
    public void kafkaContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/vectorized/redpanda");
    }

}
