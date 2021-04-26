package io.quarkus.ts.micrometer.prometheus.kafka;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class KafkaOpenShiftAlertEventsIT extends BaseOpenShiftAlertEventsIT {

    @KafkaContainer
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .onPostStart(KafkaOpenShiftAlertEventsIT::loadServiceMonitor);

    @Override
    protected RestService getApp() {
        return app;
    }
}
