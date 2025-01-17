package io.quarkus.ts.messaging.kafka.reactive.streams;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Operator;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.operator.KafkaInstance;

@OpenShiftScenario
public class OperatorOpenShiftAmqStreamsKafkaStreamIT extends BaseKafkaStreamTest {

    // TODO: use default KafkaInstance constructor when amq streams operator will support kafka 3.9.0+
    @Operator(name = "amq-streams", source = "redhat-operators")
    static KafkaInstance kafka = new KafkaInstance("kafka-instance", "/amq-streams-operator-kafka-instance.yaml");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("quarkus.kafka-streams.bootstrap-servers", kafka::getBootstrapUrl);

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
