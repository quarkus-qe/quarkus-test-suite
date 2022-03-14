package io.quarkus.ts.stork;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
