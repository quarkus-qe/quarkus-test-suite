package io.quarkus.ts.http.minimum;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class HelloResource {
    private static final String TEMPLATE = "Hello, %s!";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Hello get(@QueryParam("name") @DefaultValue("World") String name) {
        return new Hello(String.format(TEMPLATE, name));
    }

    @GET
    @Path("/json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Hello getJson() {
        return new Hello("hello");
    }
}
