package io.quarkus.ts.messaging.cloudevents.amqpjson;

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
    /**
     * We are sending String instead of Integer since json cloudevents require json body[1] and AMQP can not serialize Integer
     * into json[2]
     * [1]
     * https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.12/amqp/amqp.html#_sending_cloud_events,
     * last paragraph
     * [2] https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.12/amqp/amqp.html#_serialization
     */
    public Multi<Message<String>> generate() {
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
                    String message = String.valueOf(price);
                    return Message.of(message)
                            .addMetadata(metadata);
                });
    }
}
