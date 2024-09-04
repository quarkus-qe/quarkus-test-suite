package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/rest")
@RegisterClientHeaders
public interface RestInterface {

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    Book getAsJson();
}
