package io.quarkus.ts.messaging.kafka;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@Tag("QUARKUS-1089")
@QuarkusScenario
@Tag("podman-incompatible") //todo fails on podman 4.4.1, works on 4.5.0 and above
public class ConfluentKafkaAvroGroupIdIT extends BaseKafkaAvroGroupIdIT {

    @KafkaContainer(vendor = KafkaVendor.CONFLUENT, withRegistry = true)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-resteasy-jsonb"))
    static RestService appGroupIdA = new RestService()
            .withProperty("cron.expr", "disabled")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("confluent.registry.url", kafka::getRegistryUrl);

    @QuarkusApplication(dependencies = @Dependency(artifactId = "quarkus-resteasy-jsonb"))
    static RestService appGroupIdB = new RestService()
            .withProperty("cron.expr", "disabled")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("confluent.registry.url", kafka::getRegistryUrl);

    @Override
    public RestService getAppA() {
        return appGroupIdA;
    }

    @Override
    public RestService getAppB() {
        return appGroupIdB;
    }
}
