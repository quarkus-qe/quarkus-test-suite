package io.quarkus.ts.messaging.infinispan.grpc.kafka;

import static io.quarkus.test.services.Certificate.Format.PKCS12;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaProtocol;
import io.quarkus.test.services.containers.model.KafkaVendor;

@Tag("QUARKUS-2036")
@QuarkusScenario
public class InfinispanKafkaSaslSslIT {

    @Container(image = "${infinispan.image}", expectedLog = "${infinispan.expected-log}", port = 11222, command = "-c /infinispan-config.xml", portDockerHostToLocalhost = true)
    static final InfinispanService infinispan = new InfinispanService()
            .withConfigFile("infinispan-config.xml")
            .withSecretFiles(CertUtils.KEYSTORE);

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SASL_SSL)
    static final KafkaService kafkaSaslSsl = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperties(kafkaSaslSsl::getSslProperties)
            .withProperty("quarkus.infinispan-client.hosts", infinispan::getInfinispanServerAddress)
            .withProperty("quarkus.infinispan-client.username", infinispan.getUsername())
            .withProperty("quarkus.infinispan-client.password", infinispan.getPassword())
            .withProperty("quarkus.infinispan-client.trust-store", CertUtils.getTruststorePath())
            .withProperty("quarkus.infinispan-client.trust-store-password", CertUtils.PASSWORD)
            .withProperty("quarkus.infinispan-client.trust-store-type", PKCS12.toString())
            .withProperty("kafka-streams.state.dir", "target")
            .withProperty("kafka-client-sasl-ssl.bootstrap.servers", kafkaSaslSsl::getBootstrapUrl);

    @Test
    void testKafkaClientSSL() {
        await().untilAsserted(() -> {
            pushEvent("my-key", "my-value");
            verifyEventWasProcessed("my-key-my-value");
            pushEvent("my-key", "my-value-two");
            verifyEventWasProcessed("my-key-my-value-two");
        });

        get("/kafka/sasl-ssl/topics")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("hello"));
    }

    private void pushEvent(String key, String value) {
        given()
                .queryParam("key", key)
                .queryParam("value", value)
                .when()
                .post("/kafka/sasl-ssl")
                .then()
                .statusCode(200);
    }

    private void verifyEventWasProcessed(String expectedEvent) {
        get("/kafka/sasl-ssl")
                .then()
                .statusCode(200)
                .body(StringContains.containsString(expectedEvent));
    }
}
