package io.quarkus.qe.messaging.infinispan.kafka;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.messaging.infinispan.containers.SslStrimziKafkaTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

// TODO: change to @QuarkusScenario once https://github.com/quarkus-qe/quarkus-test-framework/issues/248 is solved
@QuarkusTest
@QuarkusTestResource(SslStrimziKafkaTestResource.class)
@Tag("fips-incompatible")
public class SslApacheKafkaClientTest {
    @Test
    void testKafkaClientSSL() {
        await().untilAsserted(() -> {
            given()
                    .queryParam("key", "my-key")
                    .queryParam("value", "my-value")
                    .when()
                    .post("/kafka/ssl")
                    .then()
                    .statusCode(200);

            get("/kafka/ssl")
                    .then()
                    .statusCode(200)
                    .body(containsString("my-key-my-value"));
        });

        get("/kafka/ssl/topics")
                .then()
                .statusCode(200)
                .body(containsString("hello"));
    }
}
