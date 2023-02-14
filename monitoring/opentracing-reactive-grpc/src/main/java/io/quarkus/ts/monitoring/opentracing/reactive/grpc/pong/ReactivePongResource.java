package io.quarkus.ts.monitoring.opentracing.reactive.grpc.pong;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.ts.monitoring.opentracing.reactive.grpc.traceable.TraceableResource;
import io.smallrye.mutiny.Uni;

@Path("/reactive-pong")
public class ReactivePongResource extends TraceableResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getPong() {
        recordTraceId();
        return Uni.createFrom().item("pong");
    }
}