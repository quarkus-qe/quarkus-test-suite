package io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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