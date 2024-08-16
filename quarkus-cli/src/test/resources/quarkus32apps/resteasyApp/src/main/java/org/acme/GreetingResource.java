package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
public class GreetingResource {

    @Inject
    @RestClient
    RestInterface restInterface;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }

    @GET
    @Path("/book")
    @Produces(MediaType.TEXT_PLAIN)
    public String getBookTitle() {
        return restInterface.getAsJson().getTitle();
    }
}
