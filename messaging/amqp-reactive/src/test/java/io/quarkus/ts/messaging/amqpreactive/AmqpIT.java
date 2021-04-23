package io.quarkus.ts.messaging.amqpreactive;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.AmqService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.AmqContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.AmqProtocol;

@QuarkusScenario
public class AmqpIT {

    static final int ASSERT_TIMEOUT_SECONDS = 30;
    static final List<String> EXPECTED_PRICES = Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100");

    @AmqContainer(image = "registry.access.redhat.com/amq-broker-7/amq-broker-72-openshift", protocol = AmqProtocol.AMQP)
    static AmqService amq = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("amqp-host", amq::getAmqpHost)
            .withProperty("amqp-port", () -> "" + amq.getPort());

    /**
     * The producer sends a price every 1 sec {@link PriceProducer#generate()}.
     * Eventually, the consumer will get up to 10 prices (from 10 to 100) but it might receive more
     * {@link PriceConsumer#process()}.
     */
    @Test
    public void testLastPrice() {
        await().pollInterval(1, TimeUnit.SECONDS)
                .atMost(ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> {
            String response = app.given().get("/price")
                    .then().statusCode(HttpStatus.SC_OK).extract().asString();
            assertTrue(EXPECTED_PRICES.stream().anyMatch(response::contains), "Expected prices not found in " + response);
        });
    }
}
