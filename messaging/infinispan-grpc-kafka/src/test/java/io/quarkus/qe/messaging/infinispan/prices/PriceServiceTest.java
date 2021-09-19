package io.quarkus.qe.messaging.infinispan.prices;

import static io.restassured.RestAssured.get;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.quarkus.qe.messaging.infinispan.containers.ConfluentKafkaTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

// TODO: change to @QuarkusScenario once https://github.com/quarkus-qe/quarkus-test-framework/issues/292 is solved
@QuarkusTest
@QuarkusTestResource(ConfluentKafkaTestResource.class)
public class PriceServiceTest {

    @Test
    public void testPricesResource() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            get("/prices/poll")
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        });

    }
}
