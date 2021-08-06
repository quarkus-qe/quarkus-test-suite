package io.quarkus.ts.messaging.amqpreactive;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public abstract class BaseAmqpIT {

    static final int ASSERT_TIMEOUT_SECONDS = 30;
    static final List<String> EXPECTED_PRICES = Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100");

    /**
     * The producer sends a price every 1 sec {@link PriceProducer#generate()}.
     * Eventually, the consumer will get up to 10 prices (from 10 to 100) but it might receive more
     * {@link PriceConsumer#process(Integer price)}.
     */
    @Test
    public void testLastPrice() {
        await().pollInterval(1, TimeUnit.SECONDS)
                .atMost(ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> {
                    String response = given().get("/price")
                            .then().statusCode(HttpStatus.SC_OK).extract().asString();
                    assertTrue(EXPECTED_PRICES.stream().anyMatch(response::contains),
                            "Expected prices not found in " + response);
                });
    }
}
