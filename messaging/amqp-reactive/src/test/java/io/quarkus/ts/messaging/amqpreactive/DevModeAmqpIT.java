package io.quarkus.ts.messaging.amqpreactive;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeAmqpIT extends BaseAmqpIT {

    /**
     * AMQP must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Test
    public void amqpContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: quay.io/artemiscloud/activemq-artemis-broker");
    }
}
