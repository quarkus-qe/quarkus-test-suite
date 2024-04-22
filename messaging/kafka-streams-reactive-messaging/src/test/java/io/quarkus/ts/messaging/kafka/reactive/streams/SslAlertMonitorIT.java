package io.quarkus.ts.messaging.kafka.reactive.streams;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaProtocol;
import io.quarkus.test.services.containers.model.KafkaVendor;

@Tag("fips-incompatible")
@QuarkusScenario
@DisabledOnRHBQandWindows(reason = "QUARKUS-3434")
public class SslAlertMonitorIT extends BaseKafkaStreamTest {
    /**
     * We can't rename this file to use the default SSL settings part of KafkaService.
     */
    private static final String TRUSTSTORE_FILE = "strimzi-server-ssl-truststore.p12";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SSL, kafkaConfigResources = TRUSTSTORE_FILE, builder = LocalHostKafkaContainerManagedResourceBuilder.class)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("kafka.security.protocol", "SSL")
            .withProperty("kafka.ssl.truststore.location", TRUSTSTORE_FILE)
            .withProperty("kafka.ssl.truststore.password", "top-secret")
            .withProperty("kafka.ssl.truststore.type", "PKCS12");

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
