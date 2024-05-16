package io.quarkus.ts.messaging.infinispan.grpc.kafka;

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

@QuarkusScenario
// TODO https://github.com/quarkusio/quarkus/issues/25136
@Tag("fips-incompatible")
public class InfinispanKafkaSaslIT {

    @Container(image = "${infinispan.image}", expectedLog = "${infinispan.expected-log}", port = 11222, command = "-c /infinispan-config.xml", portDockerHostToLocalhost = true)
    static final InfinispanService infinispan = new InfinispanService()
            .withConfigFile("infinispan-config.xml")
            .withSecretFiles("keystore.jks");

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SASL)
    static final KafkaService kafkasasl = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.infinispan-client.hosts", infinispan::getInfinispanServerAddress)
            .withProperty("quarkus.infinispan-client.username", infinispan.getUsername())
            .withProperty("quarkus.infinispan-client.password", infinispan.getPassword())
            .withProperty("quarkus.infinispan-client.trust-store", "secret::/truststore.jks")
            .withProperty("quarkus.infinispan-client.trust-store-password", "password")
            .withProperty("quarkus.infinispan-client.trust-store-type", "jks")
            .withProperty("kafka-streams.state.dir", "target")
            .withProperty("kafka-client-sasl.bootstrap.servers", kafkasasl::getBootstrapUrl);

    @Test
    void testKafkaClientSSL() {
        await().untilAsserted(() -> {
            pushEvent("my-key", "my-value");
            verifyEventWasProcessed("my-key-my-value");
            pushEvent("my-key", "my-value-two");
            verifyEventWasProcessed("my-key-my-value-two");
        });

        get("/kafka/sasl/topics")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("hello"));
    }

    private void pushEvent(String key, String value) {
        given()
                .queryParam("key", key)
                .queryParam("value", value)
                .when()
                .post("/kafka/sasl")
                .then()
                .statusCode(200);
    }

    private void verifyEventWasProcessed(String expectedEvent) {
        get("/kafka/sasl")
                .then()
                .statusCode(200)
                .body(StringContains.containsString(expectedEvent));
    }
}
