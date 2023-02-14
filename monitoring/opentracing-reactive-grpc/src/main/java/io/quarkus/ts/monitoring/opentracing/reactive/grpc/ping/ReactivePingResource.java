package io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping.clients.ReactivePongClient;
import io.quarkus.ts.monitoring.opentracing.reactive.grpc.traceable.TraceableResource;
import io.smallrye.mutiny.Uni;

@Path("/reactive-ping")
public class ReactivePingResource extends TraceableResource {

    @Inject
    @RestClient
    ReactivePongClient pongClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> getPing() {
        recordTraceId();
        return pongClient.getPong().onItem().transform(response -> "ping " + response);
    }
}