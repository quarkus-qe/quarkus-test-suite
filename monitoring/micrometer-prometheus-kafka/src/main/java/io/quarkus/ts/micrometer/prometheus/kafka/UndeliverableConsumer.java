package io.quarkus.ts.micrometer.prometheus.kafka;

import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

public class UndeliverableConsumer {
    @Incoming(Channels.CHANNEL_UNDELIVERABLE_TARGET)
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> consume(Message<String> message) {
        return message.nack(new IllegalArgumentException("Can't invoke this"));
    }
}
