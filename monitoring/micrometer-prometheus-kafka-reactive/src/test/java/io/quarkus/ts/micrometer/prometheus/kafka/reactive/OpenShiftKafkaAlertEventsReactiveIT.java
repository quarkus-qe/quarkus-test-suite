package io.quarkus.ts.micrometer.prometheus.kafka.reactive;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "debezium/zookeeper container not available on s390x.")
public class OpenShiftKafkaAlertEventsReactiveIT extends BaseOpenShiftAlertEventsReactiveIT {

    @KafkaContainer
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .onPostStart(OpenShiftKafkaAlertEventsReactiveIT::loadServiceMonitor);

    @Override
    protected RestService getApp() {
        return app;
    }
}
