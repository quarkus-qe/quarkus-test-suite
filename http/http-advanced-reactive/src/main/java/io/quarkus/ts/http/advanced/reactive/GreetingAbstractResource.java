package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/greeting")
public abstract class GreetingAbstractResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public abstract String hello();
}
