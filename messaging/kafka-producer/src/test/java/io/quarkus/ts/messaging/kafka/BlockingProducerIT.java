package io.quarkus.ts.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

@Tag("QUARKUS-1090")
@QuarkusScenario
public class BlockingProducerIT {
    private static final int TIMEOUT_SEC = 5;
    private static final int EVENTS = 100;
    static final long KAFKA_MAX_BLOCK_MS = 1000;
    static final long NETWORK_DELAY_MS = 300;
    static final long KAFKA_MAX_BLOCK_TIME_MS = KAFKA_MAX_BLOCK_MS + NETWORK_DELAY_MS;

    static CustomStrimziKafkaContainer kafkaContainer;

    @QuarkusApplication
    static RestService app = new RestService()
            .onPreStart(app -> {
                Map<String, String> kafkaProp = Map.of("auto.create.topics.enable", "false");
                kafkaContainer = new CustomStrimziKafkaContainer("0.24.0-kafka-2.7.0", kafkaProp);
                kafkaContainer.start();
            })
            .withProperty("kafka.bootstrap.servers", () -> kafkaContainer.getBootstrapServers());

    // TODO https://github.com/quarkus-qe/quarkus-test-framework/issues/248
    // Remove after all once this ticket is resolved
    @AfterAll
    public static void afterAll() {
        kafkaContainer.stop();
    }

    @Test
    public void kafkaProducerBlocksIfTopicsNotExistWithMetadata() {
        UniAssertSubscriber<Integer> subscriber = makeHttpReqAsJson("/event/tooLongToExist")
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        int reqTime = subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();
        assertTrue(reqTime < KAFKA_MAX_BLOCK_TIME_MS, getErrorMsg(reqTime));
    }

    @Test
    public void kafkaProducerBlocksIfTopicsNotExistEmitterWithoutMetadata() {
        UniAssertSubscriber<Integer> subscriber = makeHttpReqAsJson("/event")
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        int reqTime = subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();
        assertTrue(reqTime < KAFKA_MAX_BLOCK_TIME_MS, getErrorMsg(reqTime));
    }

    @Test
    public void severalEventsProducedKeepResponseTimes() {
        for (int i = 0; i < EVENTS; i++) {
            UniAssertSubscriber<Integer> subscriber = makeHttpReqAsJson("/event")
                    .subscribe().withSubscriber(UniAssertSubscriber.create());

            int reqTime = subscriber.awaitItem(Duration.ofSeconds(TIMEOUT_SEC)).getItem();
            assertTrue(reqTime < KAFKA_MAX_BLOCK_TIME_MS, getErrorMsg(reqTime));
        }
    }

    private Uni<Integer> makeHttpReqAsJson(String path) {
        return app.mutiny().postAbs(getAppEndpoint() + path).send()
                .map(resp -> Integer.parseInt(resp.getHeader("x-ms")));
    }

    private String getAppEndpoint() {
        return String.format("http://localhost:%d/", app.getPort());
    }

    private String getErrorMsg(long reqTime) {
        return String.format("reqTime %d greater than KafkaMaxBlockMs %d", reqTime, KAFKA_MAX_BLOCK_TIME_MS);
    }
}
