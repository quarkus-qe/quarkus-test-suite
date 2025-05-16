package io.quarkus.ts.stork;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/greeting")
public class GreetingResource implements IGreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Stork!";
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessageCount() {
        return String.valueOf(PriceConsumer.getCount());
    }
}