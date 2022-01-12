package io.quarkus.ts.messaging.kafka;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-959")
@Tag("QUARKUS-1087")
@QuarkusScenario
public class DevModeApicurioIT extends BaseKafkaAvroIT {
    @DevModeQuarkusApplication
    static RestService appDevMode = new RestService()
            .withProperties("devservices-application.properties");

    @Override
    public RestService getApp() {
        return appDevMode;
    }
}
