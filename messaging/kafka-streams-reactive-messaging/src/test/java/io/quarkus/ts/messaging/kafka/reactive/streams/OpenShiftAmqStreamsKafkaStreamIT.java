package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftAmqStreamsKafkaStreamIT extends BaseKafkaStreamTest {

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, image = "${amq-streams.image}", version = "${amq-streams.version}", withRegistry = true)
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("quarkus.kafka-streams.bootstrap-servers", kafka::getBootstrapUrl);

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
