package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Operator;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.operator.KafkaInstance;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1147")
public class OperatorOpenShiftAmqStreamsKafkaStreamIT extends BaseKafkaStreamTest {
    @Operator(name = "amq-streams", source = "redhat-operators")
    static KafkaInstance kafka = new KafkaInstance();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("quarkus.kafka-streams.bootstrap-servers", kafka::getBootstrapUrl);

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
