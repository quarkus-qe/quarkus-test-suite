package io.quarkus.ts.messaging.cloudevents.amqpbinary;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public abstract class BaseBinaryCloudEventsOverAmqpIT {
    static final int ASSERT_TIMEOUT_SECONDS = 30;
    static final List<String> EXPECTED_PRICES = Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100");

    /**
     * The producer sends a price every 1 sec {@link Producer#generate()}.
     * Eventually, the consumer will get up to 10 prices (from 10 to 100) but it might receive more
     */
    @Test
    public void testLastPrice() {
        await().pollInterval(1, TimeUnit.SECONDS)
                .atMost(ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> {
                    HashMap response = given().get("/result")
                            .then().statusCode(HttpStatus.SC_OK).extract().as(HashMap.class);
                    assertEquals("http://example.fake", response.get("source"));
                    assertTrue(EXPECTED_PRICES.contains(response.get("value")));
                    assertEquals("This is a price", response.get("subject"));
                });
    }
}
