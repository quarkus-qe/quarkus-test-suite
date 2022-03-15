package io.quarkus.ts.messaging.amqpreactive;

import java.net.URI;
import java.time.Duration;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;

@ApplicationScoped
public class Producer {

    private static final int TEN = 10;
    private static final int HUNDRED = 100;
    private static final Logger LOG = Logger.getLogger(Producer.class.getName());

    @Outgoing("generated-events")
    public Multi<Message<Integer>> generate() {
        LOG.info("generate fired...");
        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .onOverflow().drop()
                .map(tick -> ((tick.intValue() * TEN) % HUNDRED) + TEN)
                .map(price -> {
                    OutgoingCloudEventMetadata<Integer> metadata = OutgoingCloudEventMetadata.<Integer> builder()
                            .withType("price")
                            .withSource(URI.create("http://example.com"))
                            .withSubject("This is a price")
                            .build();
                    return Message.of(price).addMetadata(metadata);
                });
    }
}
