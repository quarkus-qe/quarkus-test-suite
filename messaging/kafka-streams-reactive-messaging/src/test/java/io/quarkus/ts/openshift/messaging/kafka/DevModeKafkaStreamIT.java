package io.quarkus.ts.openshift.messaging.kafka;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-1026")
@QuarkusScenario
@DisabledOnNativeImage
public class DevModeKafkaStreamIT extends BaseKafkaStreamIT {

    /**
     * Kafka must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Override
    public RestService getApp() {
        return app;
    }
}
