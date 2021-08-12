package io.quarkus.ts.messaging.kafka;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@QuarkusScenario
public class ConfluentKafkaAvroIT extends BaseKafkaAvroIT {

    @KafkaContainer(vendor = KafkaVendor.CONFLUENT, withRegistry = true)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperties("confluent-application.properties")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("confluent.registry.url", kafka::getRegistryUrl);

    @Override
    public RestService getApp() {
        return app;
    }
}
