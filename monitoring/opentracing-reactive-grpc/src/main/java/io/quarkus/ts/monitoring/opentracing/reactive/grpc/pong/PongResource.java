package io.quarkus.ts.monitoring.opentracing.reactive.grpc.pong;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.ts.monitoring.opentracing.reactive.grpc.traceable.TraceableResource;

@Path("/rest-pong")
public class PongResource extends TraceableResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPong() {
        recordTraceId();
        return "pong";
    }
}