package io.quarkus.ts.messaging.kafka.reactive.streams;

import static io.quarkus.ts.messaging.kafka.reactive.streams.DisabledOnWindowsWithRhbqCondition.DISABLED_IF_RHBQ_ON_WINDOWS;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@DisabledIf(value = DISABLED_IF_RHBQ_ON_WINDOWS, disabledReason = "QUARKUS-3434")
@Tag("QUARKUS-1026")
@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeStrimziKafkaStreamIT extends BaseKafkaStreamTest {

    static final String DEV_SERVICE_KAFKA_PROVIDER = "strimzi";

    /**
     * Kafka must be started using DEV services when running in DEV mode
     */
    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.kafka.devservices.provider", DEV_SERVICE_KAFKA_PROVIDER)
            .withProperty("quarkus.kafka.devservices.enabled", Boolean.TRUE.toString());

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }

    @Test
    public void kafkaContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: quay.io/strimzi-test-container/test-container");
    }
}
