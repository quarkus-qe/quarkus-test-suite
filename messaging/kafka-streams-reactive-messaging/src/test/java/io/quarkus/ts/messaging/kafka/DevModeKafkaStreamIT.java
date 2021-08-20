package io.quarkus.ts.messaging.kafka;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-1026")
@QuarkusScenario
public class DevModeKafkaStreamIT extends BaseKafkaStreamTest {

    /**
     * Kafka must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.devservices.enabled", Boolean.TRUE.toString());

    @Override
    protected String getAppUrl() {
        return app.getHost() + ":" + app.getPort();
    }
}
