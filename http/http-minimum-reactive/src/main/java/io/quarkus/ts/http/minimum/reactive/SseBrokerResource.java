package io.quarkus.ts.http.minimum.reactive;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("/sse-broker")
public class SseBrokerResource {
    @Context
    Sse sse;

    private final List<SseEventSink> sseEventSinks = new ArrayList<>();

    @GET
    @Path("read")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void read(@Context SseEventSink eventSink) {
        registerSink(eventSink);
    }

    @PUT
    @Path("send")
    public void send(String data) {
        OutboundSseEvent event = sse.newEvent(data);
        for (SseEventSink sseEventSink : sseEventSinks) {
            sseEventSink.send(event);
        }
    }

    private void registerSink(SseEventSink sseEventSink) {
        sseEventSinks.add(sseEventSink);
    }
}
