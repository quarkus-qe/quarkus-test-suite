package io.quarkus.ts.messaging.kafka.reactive.streams;

import static io.quarkus.ts.messaging.kafka.reactive.streams.DisabledOnWindowsWithRhbqCondition.DISABLED_IF_RHBQ_ON_WINDOWS;

import org.junit.jupiter.api.condition.DisabledIf;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@DisabledIf(value = DISABLED_IF_RHBQ_ON_WINDOWS, disabledReason = "QUARKUS-3434")
@QuarkusScenario
public class StrimziKafkaStreamIT extends BaseKafkaStreamTest {

    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
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
