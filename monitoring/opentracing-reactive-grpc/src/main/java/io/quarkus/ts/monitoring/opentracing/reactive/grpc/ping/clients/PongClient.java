package io.quarkus.ts.monitoring.opentracing.reactive.grpc.ping.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface PongClient {
    @GET
    @Path("/rest-pong")
    @Produces(MediaType.TEXT_PLAIN)
    String getPong();

}
