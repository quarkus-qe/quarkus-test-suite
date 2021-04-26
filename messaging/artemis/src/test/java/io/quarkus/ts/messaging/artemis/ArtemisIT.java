package io.quarkus.ts.messaging.artemis;

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

@QuarkusScenario
public class ArtemisIT {

    static final int ASSERT_TIMEOUT_SECONDS = 60;

    @AmqContainer(image = "registry.access.redhat.com/amq-broker-7/amq-broker-72-openshift")
    static AmqService artemis = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService().withProperty("quarkus.artemis.username", artemis.getAmqUser())
            .withProperty("quarkus.artemis.password", artemis.getAmqPassword())
            .withProperty("quarkus.artemis.url", artemis::getTcpUrl);

    /**
     * There is a PriceProducer that pushes a new integer "price" to a JMS queue
     * called "prices" each second. PriceConsumer is a loop that starts at the
     * beginning of the application runtime and blocks on reading from the queue
     * called "prices". Once a value is read, the attribute lastPrice is updated.
     * <p>
     * This test merely checks that the value was updated. It is the most basic
     * sanity check that JMS is up and running.
     */
    @Test
    public void testLastPrice() {
        await().atMost(ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> {
            String value = app.given().get("/prices/last").then().statusCode(HttpStatus.SC_OK).extract().body().asString();

            int intValue = Integer.parseInt(value);
            assertThat(intValue, greaterThanOrEqualTo(0));
            assertThat(intValue, lessThan(PriceProducer.PRICES_MAX));
        });
    }
}
