package io.quarkus.ts.opentelemetry.reactive.sse;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.opentelemetry.reactive.traceable.TraceableResource;
import io.smallrye.mutiny.Multi;

@Path("/server-sent-events-ping")
public class ServerSentEventsPingResource extends TraceableResource {

    @Inject
    @RestClient
    ServerSentEventsPongClient pongClient;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> getPing() {
        recordTraceId();
        return pongClient.getPong().map(response -> "ping " + response);
    }

    @Path("/raw")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<OutboundSseEvent> sseRaw(@Context Sse sse, @QueryParam("amount") int amount) {
        List<OutboundSseEvent> events = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            events.add(sse.newEventBuilder().id("id_" + i).data("data_" + i).name("name_" + i).build());
        }

        return Multi.createFrom().items(events.toArray(OutboundSseEvent[]::new));
    }
}
