package io.quarkus.qe.messaging.ssl;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaProtocol;
import io.quarkus.test.services.containers.model.KafkaVendor;

@QuarkusScenario
// TODO https://github.com/quarkusio/quarkus/issues/25136
@Tag("fips-incompatible")
public class KafkaSslIT {

    /**
     * We can't rename this file to use the default SSL settings part of KafkaService.
     */
    private static final String TRUSTSTORE_FILE = "strimzi-server-ssl-truststore.p12";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, protocol = KafkaProtocol.SSL, kafkaConfigResources = TRUSTSTORE_FILE)
    static final KafkaService kafkassl = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafkassl::getBootstrapUrl)
            .withProperty("kafka.ssl.enable", "true")
            .withProperty("kafka.ssl.truststore.location", TRUSTSTORE_FILE)
            .withProperty("kafka.ssl.truststore.password", "top-secret")
            .withProperty("kafka.ssl.truststore.type", "PKCS12")
            .withProperty("kafka-streams.state.dir", "target")
            .withProperty("kafka-client-ssl.bootstrap.servers", kafkassl::getBootstrapUrl);

    @Test
    void testKafkaClientSSL() {
        await().untilAsserted(() -> {
            pushEvent("my-key", "my-value");
            verifyEventWasProcessed("my-key-my-value");
            pushEvent("my-key", "my-value-two");
            verifyEventWasProcessed("my-key-my-value-two");
        });

        get("/kafka/ssl/topics")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("hello"));
    }

    private void pushEvent(String key, String value) {
        given()
                .queryParam("key", key)
                .queryParam("value", value)
                .when()
                .post("/kafka/ssl")
                .then()
                .statusCode(200);
    }

    private void verifyEventWasProcessed(String expectedEvent) {
        get("/kafka/ssl")
                .then()
                .statusCode(200)
                .body(StringContains.containsString(expectedEvent));
    }
}
