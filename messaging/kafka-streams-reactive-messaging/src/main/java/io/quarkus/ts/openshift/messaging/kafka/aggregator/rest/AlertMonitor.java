package io.quarkus.ts.openshift.messaging.kafka.aggregator.rest;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class AlertMonitor {

    @Inject
    @Channel("login-alerts")
    Publisher<String> alerts;

    @GET
    public Response ok() {
        return Response.ok().build();
    }

    @GET
    @Path("/monitor/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("application/json")
    public Publisher<String> stream() {
        return alerts;
    }

}
