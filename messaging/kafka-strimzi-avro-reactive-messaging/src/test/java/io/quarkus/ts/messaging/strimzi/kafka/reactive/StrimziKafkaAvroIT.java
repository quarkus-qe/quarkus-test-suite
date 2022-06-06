package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@QuarkusScenario
// TODO https://github.com/quarkusio/quarkus/issues/25814
@DisabledOnQuarkusSnapshot(reason = "Quarkus upstream is using Apicurio 2.2.3.Final that is not backward compatible with Apicurio 2.1.5 used previously")
public class StrimziKafkaAvroIT extends BaseKafkaAvroIT {

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, withRegistry = true, registryPath = "/apis/registry/v2")
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("strimzi-application.properties")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("kafka.registry.url", kafka::getRegistryUrl);

    @Override
    public RestService getApp() {
        return app;
    }
}
