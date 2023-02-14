package io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
public interface ReactivePongClient {
    @GET
    @Path("/reactive-pong")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> getPong();

}
