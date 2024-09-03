package io.quarkus.ts.micrometer.prometheus.kafka;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("/undeliverable")
public class UndeliverableProducer {
    @Channel(Channels.CHANNEL_UNDELIVERABLE_SOURCE)
    Emitter<String> dataEmitter;

    @GET
    public void sendData() {
        dataEmitter.send("random data");
    }
}
