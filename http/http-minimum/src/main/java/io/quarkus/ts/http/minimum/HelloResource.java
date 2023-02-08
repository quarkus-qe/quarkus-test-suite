package io.quarkus.ts.http.minimum;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @GET
    @Path("/foo/{something-with-dash:[A-Z0-9]{4}}")
    public Response doSomething(@PathParam("something-with-dash") String param) {
        return Response.noContent().build();
    }

    @GET
    @Path("/no-content-length")
    public Response hello() {
        return Response.ok("hello").header("Transfer-Encoding", "chunked").build();
    }
}
