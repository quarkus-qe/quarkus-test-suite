package io.quarkus.ts.openshift.messaging.kafka;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftAmqStreamsKafkaStreamIT extends BaseKafkaStreamIT {
    @KafkaContainer(vendor = KafkaVendor.STRIMZI,
            image = "registry.redhat.io/amq7/amq-streams-kafka-24-rhel7",
            version = "1.5.0",
            withRegistry = true)
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Override
    public RestService getApp() {
        return app;
    }
}
