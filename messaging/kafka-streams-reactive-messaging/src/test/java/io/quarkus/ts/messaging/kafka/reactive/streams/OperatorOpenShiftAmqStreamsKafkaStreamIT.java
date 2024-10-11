package io.quarkus.ts.messaging.kafka.reactive.streams;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnAarch64Native;
import io.quarkus.test.services.Operator;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.operator.KafkaInstance;

@OpenShiftScenario
@DisabledOnAarch64Native(reason = "https://issues.redhat.com/browse/QUARKUS-4321")
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
