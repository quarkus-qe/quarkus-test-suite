package io.quarkus.ts.messaging.qpid;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

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
public class QpidIT {

    static final int ASSERT_TIMEOUT_MINUTES = 1;

    @AmqContainer(image = "${amqbroker.image}", protocol = AmqProtocol.AMQP)
    static AmqService artemis = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService().withProperty("quarkus.qpid-jms.username", artemis.getAmqUser())
            .withProperty("quarkus.qpid-jms.password", artemis.getAmqPassword())
            .withProperty("quarkus.qpid-jms.url", artemis::getAmqpUrl);

    @Test
    public void testLastPrice() {
        await().atMost(ASSERT_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String value = app.given()
                    .get("/prices/last")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().body().asString();

            int intValue = Integer.parseInt(value);
            assertThat(intValue, greaterThanOrEqualTo(0));
            assertThat(intValue, lessThan(PriceProducer.PRICES_MAX));
        });
    }
}
