package io.quarkus.ts.messaging.kafka.producer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

// This change should not trigger any tests in GHPRB workflow.
@Tag("QUARKUS-1090")
@QuarkusScenario
public class BlockingProducerIT {
    private static final int TIMEOUT_SEC = 5;
    private static final int EVENTS = 100;
    static final long KAFKA_MAX_BLOCK_MS = 1000;
    static final long NETWORK_DELAY_MS = 300;
    static final long KAFKA_MAX_BLOCK_TIME_MS = KAFKA_MAX_BLOCK_MS + NETWORK_DELAY_MS;

    @KafkaContainer(vendor = KafkaVendor.STRIMZI, version = "0.24.0-kafka-2.7.0")
    static final KafkaService kafka = new KafkaService().withProperty("auto.create.topics.enable", "false");

    @QuarkusApplication
    static RestService app = new RestService().withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl);

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
        return String.format("http://localhost:%d/", app.getURI().getPort());
    }

    private String getErrorMsg(long reqTime) {
        return String.format("reqTime %d greater than KafkaMaxBlockMs %d", reqTime, KAFKA_MAX_BLOCK_TIME_MS);
    }
}
