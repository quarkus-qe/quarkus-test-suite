package io.quarkus.ts.messaging.kafka.reactive.streams;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.jboss.logmanager.Level;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KafkaService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.KafkaContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.KafkaVendor;
import io.quarkus.ts.messaging.kafka.reactive.streams.shutdown.SlowTopicConsumer;
import io.quarkus.ts.messaging.kafka.reactive.streams.shutdown.SlowTopicResource;

@QuarkusScenario
@DisabledOnNative(reason = "Due to high native build execution time")
public class KafkaGratefulShutdownIT {

    private static final int TOTAL_MESSAGES = 10;
    private static final String GRATEFUL_SHUTDOWN_PROPERTY = "mp.messaging.incoming.slow.graceful-shutdown";
    private static final String KAFKA_LOG_PROPERTY = "quarkus.log.category.\"io.smallrye.reactive.messaging.kafka\".level";
    private static final String LAST_MESSAGE_LOG = "Processed Message " + TOTAL_MESSAGES;
    private static final String SHUTDOWN_KAFKA_LOG = "Shutting down - Waiting for message processing to complete";

    @KafkaContainer(vendor = KafkaVendor.STRIMZI)
    static KafkaService kafka = new KafkaService();

    @QuarkusApplication(classes = { SlowTopicConsumer.class,
            SlowTopicResource.class }, properties = "kafka.grateful.shutdown.application.properties")
    static RestService app = new RestService()
            .withProperty("kafka.bootstrap.servers", kafka::getBootstrapUrl)
            .withProperty("quarkus.kafka-streams.bootstrap-servers", kafka::getBootstrapUrl)
            .withProperty(KAFKA_LOG_PROPERTY, Level.DEBUG.getName());

    @Test
    public void shouldWaitForMessagesWhenGratefulShutdownIsEnabled() {
        givenApplicationWithGratefulShutdownEnabled();
        givenMessagesInTopic();
        whenStopApplication();
        thenAllMessagesAreProcessedOrKafkaIsShutdown();
    }

    @Test
    public void shouldNotWaitForMessagesWhenGratefulShutdownIsDisabled() {
        givenApplicationWithGratefulShutdownDisabled();
        givenMessagesInTopic();
        whenStopApplication();
        thenAllMessagesAreNotProcessed();
    }

    private void givenApplicationWithGratefulShutdownEnabled() {
        app.stop();
        app.withProperty(GRATEFUL_SHUTDOWN_PROPERTY, Boolean.TRUE.toString());
        app.start();
    }

    private void givenApplicationWithGratefulShutdownDisabled() {
        app.stop();
        app.withProperty(GRATEFUL_SHUTDOWN_PROPERTY, Boolean.FALSE.toString());
        app.start();
    }

    private void givenMessagesInTopic() {
        app.given().post("/slow-topic/sendMessages/" + TOTAL_MESSAGES);
    }

    private void whenStopApplication() {
        app.stop();
    }

    private void thenAllMessagesAreProcessedOrKafkaIsShutdown() {
        thenAssertLogs(line -> line.contains(LAST_MESSAGE_LOG) || line.contains(SHUTDOWN_KAFKA_LOG),
                "Expected output was not found in logs");
    }

    private void thenAllMessagesAreNotProcessed() {
        thenLogsDoNotContain(LAST_MESSAGE_LOG);
        thenLogsDoNotContain(SHUTDOWN_KAFKA_LOG);
    }

    private void thenLogsDoNotContain(String expectedOutput) {
        thenAssertLogs(line -> !line.contains(expectedOutput), "Unexpected output was found in logs");
    }

    private void thenAssertLogs(Predicate<String> assertion, String message) {
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> elements = app.getLogs();

            assertTrue(elements.stream().anyMatch(assertion), message + ":" + elements);
        });
    }
}
