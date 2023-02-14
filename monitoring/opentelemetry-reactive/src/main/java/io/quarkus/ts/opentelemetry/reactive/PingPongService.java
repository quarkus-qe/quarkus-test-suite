package io.quarkus.ts.opentelemetry.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@RegisterRestClient
public interface PingPongService {

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> getPongResponse();
}
