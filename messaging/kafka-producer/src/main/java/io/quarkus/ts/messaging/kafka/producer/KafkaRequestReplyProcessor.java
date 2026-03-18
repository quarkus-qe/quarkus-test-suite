package io.quarkus.ts.messaging.kafka.producer;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class KafkaRequestReplyProcessor {

    @Incoming("request")
    @Outgoing("reply")
    public String process(String text) {
        return text.toUpperCase();
    }

    @Incoming("inmess")
    @Outgoing("outmess")
    public String processMessage(String value) {
        return value.toLowerCase();
    }
}
