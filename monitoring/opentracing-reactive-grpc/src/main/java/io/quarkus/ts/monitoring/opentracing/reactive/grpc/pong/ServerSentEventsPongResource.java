package io.quarkus.ts.monitoring.opentracing.reactive.grpc.pong;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.ts.monitoring.opentracing.reactive.grpc.traceable.TraceableResource;
import io.smallrye.mutiny.Multi;

@Path("/server-sent-events-pong")
public class ServerSentEventsPongResource extends TraceableResource {

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> getPong() {
        recordTraceId();
        return Multi.createFrom().item("pong");
    }
}