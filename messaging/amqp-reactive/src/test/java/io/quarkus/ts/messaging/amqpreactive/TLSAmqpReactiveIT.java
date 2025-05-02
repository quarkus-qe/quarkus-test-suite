package io.quarkus.ts.messaging.amqpreactive;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.AmqService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.AmqContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.AmqProtocol;

/**
 * Test AMQP connection with TLS configuration where ALPN is explicitly disabled.
 * This test verifies that the fix for UnsupportedOperationException in TlsConfigUtils
 * <a href="https://github.com/quarkusio/quarkus/pull/46900">...</a>
 * when setting ALPN to false works correctly.
 */
@QuarkusScenario
public class TLSAmqpReactiveIT {

    private static final Duration ASSERT_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration READINESS_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(1);
    private static final String EXCEPTION_TO_AVOID = "UnsupportedOperationException";

    @AmqContainer(image = "${amqbroker.image}", protocol = AmqProtocol.AMQP)
    static AmqService amq = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("amqp-host", amq::getAmqpHost)
            .withProperty("amqp-port", () -> "" + amq.getPort())
            .withProperty("quarkus.tls.amqp-tls.alpn", "false")
            .withProperty("mp.messaging.incoming.prices.tls-configuration-name", "amqp-tls")
            .withProperty("mp.messaging.outgoing.generated-price.tls-configuration-name", "amqp-tls")
            .withProperty("quarkus.log.category.\"io.quarkus.tls\"", "DEBUG")
            .withProperty("quarkus.log.level", "DEBUG");

    @Test
    public void testTlsMessagingWithAlpnDisabled() {
        await().atMost(READINESS_TIMEOUT)
                .ignoreExceptions()
                .until(() -> {
                    int statusCode = app.given().get("/").statusCode();
                    return statusCode == 200;
                });

        await().atMost(ASSERT_TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> {
                    app.given()
                            .when().get("/price")
                            .then()
                            .statusCode(200);
                });
        app.logs().assertDoesNotContain(EXCEPTION_TO_AVOID);
    }
}
