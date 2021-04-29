package io.quarkus.ts.openshift.messaging.kafka.aggregator.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import io.quarkus.ts.openshift.messaging.kafka.aggregator.streams.WindowedLoginDeniedStream;

@Path("/")
public class AlertMonitor {

    @Inject
    @Channel(WindowedLoginDeniedStream.LOGIN_ALERTS_TOPIC)
    Publisher<String> alerts;

    @GET
    @Path("/monitor/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("application/json")
    public Publisher<String> stream() {
        return alerts;
    }

}
