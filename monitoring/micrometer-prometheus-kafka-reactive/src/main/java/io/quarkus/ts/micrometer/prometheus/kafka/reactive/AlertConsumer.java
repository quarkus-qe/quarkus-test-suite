package io.quarkus.ts.micrometer.prometheus.kafka.reactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.reactivestreams.Publisher;

@Path("/")
public class AlertConsumer {

    @Inject
    @Channel(Channels.ALERTS_STREAM)
    Publisher<String> alerts;

    @GET
    @Path("/monitor/stream")
    @RestStreamElementType(MediaType.TEXT_PLAIN)
    public Publisher<String> stream() {
        return alerts;
    }

}
