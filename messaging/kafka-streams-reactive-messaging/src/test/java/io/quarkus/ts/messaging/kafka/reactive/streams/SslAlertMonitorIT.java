package io.quarkus.ts.messaging.kafka.reactive.streams;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnRHBQandWindows;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaProtocol;
import io.quarkus.test.services.containers.model.KafkaVendor;

@QuarkusScenario
@DisabledOnRHBQandWindows(reason = "QUARKUS-3434")
public class SslAlertMonitorIT extends BaseKafkaStreamTest {

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SSL)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperties(kafka::getSslProperties)
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
