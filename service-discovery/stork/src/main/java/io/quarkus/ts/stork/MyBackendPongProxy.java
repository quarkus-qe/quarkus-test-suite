package io.quarkus.ts.stork;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

import io.smallrye.mutiny.Uni;

@Path("/pong")
@RegisterRestClient(baseUri = "stork://pong")
public interface MyBackendPongProxy {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    Uni<RestResponse<String>> get();
}
