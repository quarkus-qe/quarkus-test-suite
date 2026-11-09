package io.quarkus.ts.messaging.amqpreactive;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.FixedHostPortGenericContainer;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.AmqProtocol;

@QuarkusScenario
@Tag("QUARKUS-5978")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisabledIfSystemProperty(named = "gh-action-disable-on-win", matches = "true", disabledReason = "Containers are not available")
public class RestartAmqpIT {

    private static FixedHostPortGenericContainer<?> amq;
    private static final int AMQ_PORT;

    static {
        // this tests requires restart of amqp without application restart
        // Quarkus test framework restarts container on a diffent port, so the app cannot reconnect
        // and the framework doesn't support fixed ports, so we have to use testcontainers directly
        String image = ConfigProvider.getConfig().getValue("amqbroker.image", String.class);
        int portNumber = AmqProtocol.AMQP.getPort();
        AMQ_PORT = 10_000 + portNumber;
        amq = new FixedHostPortGenericContainer<>(image)
                .withEnv("AMQ_USER", "quarkus")
                .withEnv("AMQ_PASSWORD", "quarkus")
                .withFixedExposedPort(AMQ_PORT, portNumber);
        amq.start();
    }

    @QuarkusApplication(classes = { GeneratorResource.class, ManualPriceProducer.class })
    static RestService sender = new RestService()
            .withProperty("mp.messaging.outgoing.generated-price.lazy-client", "false")
            .withProperty("amqp-host", () -> amq.getHost())
            .withProperty("amqp-port", () -> "" + AMQ_PORT);

    @QuarkusApplication(classes = { PriceConsumer.class, PriceResource.class })
    static RestService receiver = new RestService()
            .withProperty("amqp-host", () -> amq.getHost())
            .withProperty("amqp-port", () -> "" + AMQ_PORT);

    @Test
    @Order(1)
    public void sendAndReceivePrice() {
        sender.given().post("send-prices/7");
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            String response = receiver.given().get("/price")
                    .then().statusCode(HttpStatus.SC_OK).extract().asString();
            assertTrue(response.contains("7"));
        });
    }

    @Test
    @Order(2)
    public void restart() {
        // restart amqp
        amq.stop();
        amq.start();
        // receiver should reconnect automatically
        await().atMost(Duration.ofSeconds(30)).ignoreExceptions().until(() -> reconnected(receiver));
        // sender should reconnect automatically because of lazy-client=false option
        await().atMost(Duration.ofSeconds(30))
                .until(() -> reconnected(sender));
    }

    private static boolean reconnected(RestService service) {
        boolean afterStart = false;
        for (String line : service.getLogs()) {
            if (!afterStart && line.contains("Connection disconnected")) {
                afterStart = true;
            }
            if (afterStart && line.contains("Connection with AMQP broker established")) {
                return true;
            }
        }
        return false;
    }
}
