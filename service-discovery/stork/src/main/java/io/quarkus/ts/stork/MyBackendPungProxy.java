package io.quarkus.ts.stork;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@Path("/pung")
@RegisterRestClient(baseUri = "stork://pung")
public interface MyBackendPungProxy {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    Uni<String> get();
}
