package io.quarkus.ts.messaging.artemisjta;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.AmqService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.AmqContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Synopsis:
 * There are three JMS queues, custom-prices-1 and custom-prices-2 are used to test
 * a transactional write: either both are correctly updated with a new value or none of them is.
 * <p>
 * custom-prices-cack queue is used to check that messages remains waiting in the queue until
 * client "acks" them, i.e. acknowledges their processing.
 */
@QuarkusScenario
public class ArtemisJtaIT {

    static final int ASSERT_TIMEOUT_SECONDS = 10;
    static final Logger LOG = Logger.getLogger(ArtemisJtaIT.class.getName());

    @AmqContainer(image = "registry.access.redhat.com/amq-broker-7/amq-broker-72-openshift")
    static AmqService artemis = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService().withProperty("quarkus.artemis.username", artemis.getAmqUser())
            .withProperty("quarkus.artemis.password", artemis.getAmqPassword())
            .withProperty("quarkus.artemis.url", artemis::getTcpUrl);

    /**
     * Number 666 is written to both queues custom-prices-1 and custom-prices-2 within a transaction.
     * The consumer is reading both queues each second and it should at some point see these messages
     * and report 666 from both of them. There is no injected failure here.
     */
    @Test
    public void testPrice() {
        app.given()
                .queryParam("fail", "false")
                .body("666")
                .when()
                .post("/price-tx")
                .then()
                .statusCode(HttpStatus.SC_OK);

        thenPriceOneIs("666");
        thenPriceTwoIs("666");
    }

    /**
     * As above, except there is an error between writing to custom-prices-1 and custom-prices-2 now.
     * Being wrapped in a transaction, it is expected that the write to custom-prices-1 is not committed
     * and no value was written to either of those queues.
     */
    @Test
    public void testJTAPriceFail() {
        app.given().queryParam("fail", "true")
                .body("999")
                .when()
                .post("/price-tx")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        thenPriceOneIs("");
        thenPriceTwoIs("");
    }

    /**
     * Continuation of the above. This time there is no transaction and there is an error
     * between writing to both queues. Not being wrapped in a transaction means that there will
     * be an expected inconsistency: one queue gets updated while the other doesn't.
     */
    @Test
    public void testPriceFail() {
        app.given()
                .queryParam("fail", "true")
                .body("69")
                .when()
                .post("/price-non-tx")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        thenPriceOneIs("69");
        thenPriceTwoIs("");
    }

    /**
     * Above examples use autoack, i.e. a message is assumed acked as soon as it is read from the queue.
     * Here we rely on the consumer, on the client to explicitly ack each message read from the queue.
     * The tests checks that even after reading them, all messages remain in the queue until we ack them.
     */
    @Test
    public void testClientAck() {
        List<String> initial = Arrays.asList("96-0", "96-1", "96-2", "96-3", "96-4");
        initial.forEach(p -> app.given().body(p).when().post("/noAck").then().statusCode(HttpStatus.SC_OK));
        clientAck(initial.size(), Arrays.asList("96-0", "96-0", "96-0", "96-0", "96-0"), false);
        clientAck(initial.size(), Arrays.asList("96-0", "96-1", "96-2", "96-3", "96-4"), true);
    }

    private void clientAck(int size, List<String> expected, boolean ack) {
        List<String> actual = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            actual.add(
                    app.given()
                            .queryParam("ack", ack)
                            .when()
                            .get("/noAck")
                            .then()
                            .statusCode(HttpStatus.SC_OK)
                            .extract().body().asString());
        }
        LOG.info("Retrieved data from queues: " + actual);
        assertThat("Expected list " + expected + " does not match th actual one: " + actual,
                expected.equals(actual));
    }

    private void thenPriceOneIs(String expected) {
        thenPriceIs("/price-1", expected);
    }

    private void thenPriceTwoIs(String expected) {
        thenPriceIs("/price-2", expected);
    }

    private void thenPriceIs(String pricePath, String expected) {
        await().pollInterval(1, TimeUnit.SECONDS)
                .atMost(ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> app.given().when()
                        .get(pricePath)
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .body(equalTo(expected)));
    }
}
