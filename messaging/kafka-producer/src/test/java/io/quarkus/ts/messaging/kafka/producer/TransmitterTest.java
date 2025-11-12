package io.quarkus.ts.messaging.kafka.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

@QuarkusTest
@WithTestResource(KafkaCompanionResource.class)
@Tag("https://issues.redhat.com/browse/QQE-1979")
@DisabledOnQuarkusVersion(version = "3.27.0.*", reason = "The issue was fixed in 3.27.1")
@DisabledOnOs({ OS.WINDOWS }) // Kafka requires docker to start
class TransmitterTest {

    @Inject
    MessageTransmitter messageTransmitter;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    void dashedTopic() {
        String message = "Hello, Quarkus!";
        messageTransmitter.emit(message);

        List<String> actual = companion.consumeStrings()
                .fromTopics("foo.bar-topic")
                .awaitRecords(1, Duration.ofSeconds(30))
                .stream()
                .map(ConsumerRecord::value)
                .toList();
        assertEquals(1, actual.size());
        assertEquals('"' + message + '"', actual.get(0));
    }
}
