package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@Tag("QUARKUS-1089")
@QuarkusScenario
// TODO https://github.com/quarkusio/quarkus/issues/25814
@DisabledOnQuarkusSnapshot(reason = "Quarkus upstream is using Apicurio 2.2.3.Final that is not backward compatible with Apicurio 2.1.5 used previously")
public class StrimziKafkaAvroGroupIdIT extends BaseKafkaAvroGroupIdIT {

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, withRegistry = true, registryPath = "/apis/registry/v2")
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService appGroupIdA = new RestService()
            .withProperties("strimzi-application.properties")
            .withProperty("cron.expr", "0 0 0 ? * * *")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("kafka.registry.url", kafka::getRegistryUrl);

    @QuarkusApplication
    static RestService appGroupIdB = new RestService()
            .withProperties("strimzi-application.properties")
            .withProperty("cron.expr", "0 0 0 ? * * *")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("kafka.registry.url", kafka::getRegistryUrl);

    @Override
    public RestService getAppA() {
        return appGroupIdA;
    }

    @Override
    public RestService getAppB() {
        return appGroupIdB;
    }
}
