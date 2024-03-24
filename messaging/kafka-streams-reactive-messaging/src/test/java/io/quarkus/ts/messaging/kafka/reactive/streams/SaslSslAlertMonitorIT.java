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
public class SaslSslAlertMonitorIT extends BaseKafkaStreamTest {
    /**
     * We can't rename this file to use the default SSL settings part of KafkaService.
     */
    private static final String TRUSTSTORE_FILE = "strimzi-server-ssl-truststore.p12";

    private final static String SASL_USERNAME_VALUE = "client";
    private final static String SASL_PASSWORD_VALUE = "client-secret";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SASL_SSL, kafkaConfigResources = TRUSTSTORE_FILE, builder = LocalHostKafkaContainerManagedResourceBuilder.class)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("kafka.security.protocol", "SASL_SSL")
            .withProperty("kafka.ssl.truststore.location", TRUSTSTORE_FILE)
            .withProperty("kafka.ssl.truststore.password", "top-secret")
            .withProperty("kafka.ssl.truststore.type", "PKCS12")
            .withProperty("kafka.sasl.mechanism", "PLAIN")
            .withProperty("kafka.sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required "
                    + "username=\"" + SASL_USERNAME_VALUE + "\" "
                    + "password=\"" + SASL_PASSWORD_VALUE + "\";");

    @Override
    protected String getAppUrl() {
        return app.getURI(Protocol.HTTP).toString();
    }
}
