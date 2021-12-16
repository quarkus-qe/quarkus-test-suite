package io.quarkus.ts.micrometer.prometheus.kafka.reactive;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestSseElementType;
import org.reactivestreams.Publisher;

@Path("/")
public class AlertConsumer {

    @Inject
    @Channel(Channels.ALERTS_STREAM)
    Publisher<String> alerts;

    @GET
    @Path("/monitor/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.TEXT_PLAIN)
    public Publisher<String> stream() {
        return alerts;
    }

}
