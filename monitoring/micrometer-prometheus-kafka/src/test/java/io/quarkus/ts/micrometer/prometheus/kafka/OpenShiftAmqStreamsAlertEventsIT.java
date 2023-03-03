package io.quarkus.ts.micrometer.prometheus.kafka;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;

// I just want to trigger a matrix build in Jenkins workflow.
@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftAmqStreamsAlertEventsIT extends BaseOpenShiftAlertEventsIT {

    @KafkaContainer(image = "${amq-streams.image}", version = "${amq-streams.version}")
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .onPostStart(OpenShiftAmqStreamsAlertEventsIT::loadServiceMonitor);

    @Override
    protected RestService getApp() {
        return app;
    }
}
