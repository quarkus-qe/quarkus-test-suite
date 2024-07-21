package io.quarkus.ts.messaging.strimzi.kafka.reactive;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "debezium/zookeeper container not available on s390x & ppc64le.")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftAmqStreamsKafkaAvroIT extends BaseKafkaAvroIT {

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, image = "${amq-streams.image}", version = "${amq-streams.version}", withRegistry = true, registryPath = "/apis/registry/v2")
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
