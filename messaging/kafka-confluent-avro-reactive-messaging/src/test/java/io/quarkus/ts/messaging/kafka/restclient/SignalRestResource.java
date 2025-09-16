package io.quarkus.ts.messaging.kafka.restclient;

import java.time.Duration;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;

@Path("/signal")
public class SignalRestResource {

    private static volatile String lastProcessedSignal;

    @RestClient
    UppercaseRestClient uppercaseRestClient;

    @Channel("signal")
    MutinyEmitter<String> signalEmitter;

    /**
     * Endpoint to generate a new sendSignalEventToKafka
     * and send it to "signal" Kafka topic using the emitter.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    public Uni<String> sendSignalEventToKafka() {
        String uuid = UUID.randomUUID().toString();
        return signalEmitter.send(uuid)
                .map(x -> uuid);
    }

    /**
     * Signal Kafka consumer
     */
    @Incoming("signal")
    @Blocking
    public void consumeSignalEvent(String signal) {
        lastProcessedSignal = uppercaseRestClient.toUppercase(signal)
                .await().atMost(Duration.ofSeconds(30));
    }

    @GET
    @Path("/last")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastProcessedSignal() {
        return lastProcessedSignal;
    }
}
