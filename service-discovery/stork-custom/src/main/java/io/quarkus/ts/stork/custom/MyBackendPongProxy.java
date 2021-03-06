package io.quarkus.ts.stork.custom;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
