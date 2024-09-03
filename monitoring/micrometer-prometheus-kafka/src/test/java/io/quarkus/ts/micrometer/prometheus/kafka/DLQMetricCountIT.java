package io.quarkus.ts.micrometer.prometheus.kafka;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Test that messages send to a dead-letter-queue are counted in micrometer metrics.
 * See links for more details:
 * - https://issues.redhat.com/browse/QUARKUS-4120
 * - https://github.com/smallrye/smallrye-reactive-messaging/issues/2473
 */
@QuarkusScenario
@Tag("QUARKUS-4120")
public class DLQMetricCountIT {
    private static final String DEAD_LETTER_TOPIC_NAME = "dead-letter-topic-undeliverable-target";
    private static final String MESSAGES_SEND_METRIC = "kafka_producer_topic_record_send_total";
    private static final String UNDELIVERABLE_ENDPOINT = "undeliverable";
    private static final int NUMBER_OF_MESSAGES_SEND = 3;

    @KafkaContainer
    static final KafkaService kafka = new KafkaService();

    @QuarkusApplication
    static RestService app = new RestService().withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

    @Test
    public void testDeadLetterMessagesAreCounted() {
        // wait for kafka to be fully loaded
        // if we send requests (messages) too soon, they will fail to propagate into topic
        kafka.logs().assertContains("Assignment received from leader kafka-consumer-undeliverable-target");

        // send requests
        for (int i = 0; i < NUMBER_OF_MESSAGES_SEND; i++) {
            app.given().get(UNDELIVERABLE_ENDPOINT).then().statusCode(HttpStatus.SC_NO_CONTENT);
        }

        thenMetricIsExposedInServiceEndpoint(MESSAGES_SEND_METRIC, DEAD_LETTER_TOPIC_NAME,
                greaterOrEqual(NUMBER_OF_MESSAGES_SEND));
    }

    private void thenMetricIsExposedInServiceEndpoint(String name, String key, Predicate<Double> valueMatcher) {
        await().ignoreExceptions().atMost(60, TimeUnit.SECONDS).untilAsserted(() -> {
            String response = app.given().get("/q/metrics").then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().asString();

            for (String line : response.split("[\r\n]+")) {
                if (line.startsWith(name) && line.contains(key)) {
                    Double value = extractValueFromMetric(line);
                    assertTrue(valueMatcher.test(value), "Metric value is not expected. Found: " + value);
                    return;
                }
            }

            fail("Metric " + name + " not found in " + response);
        });
    }

    private Predicate<Double> greaterOrEqual(double expected) {
        return actual -> actual >= expected;
    }

    private Double extractValueFromMetric(String line) {
        return Double.parseDouble(line.substring(line.lastIndexOf(" ")));
    }
}
