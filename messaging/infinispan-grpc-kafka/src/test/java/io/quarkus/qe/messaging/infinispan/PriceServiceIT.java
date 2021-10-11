package io.quarkus.qe.messaging.infinispan;

import static io.restassured.RestAssured.get;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.InfinispanService;
import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;

@QuarkusScenario
public class PriceServiceIT {

    @Container(image = "${infinispan.image}", expectedLog = "${infinispan.expected-log}", port = 11222)
    static final InfinispanService infinispan = new InfinispanService()
            .withConfigFile("infinispan-config.yaml")
            .withSecretFiles("server.jks");

    @KafkaContainer(vendor = KafkaVendor.CONFLUENT)
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.infinispan-client.server-list", infinispan::getInfinispanServerAddress)
            .withProperty("quarkus.infinispan-client.auth-username", infinispan.getUsername())
            .withProperty("quarkus.infinispan-client.auth-password", infinispan.getPassword())
            .withProperty("quarkus.infinispan-client.trust-store", "secret::/server.jks")
            .withProperty("quarkus.infinispan-client.trust-store-password", "changeit")
            .withProperty("quarkus.infinispan-client.trust-store-type", "jks")
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void testPricesResource() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            get("/prices/poll")
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        });
    }
}
