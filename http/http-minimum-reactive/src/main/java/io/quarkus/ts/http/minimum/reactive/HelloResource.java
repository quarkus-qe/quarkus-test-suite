package io.quarkus.ts.http.minimum.reactive;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/hello")
public class HelloResource {
    private static final String TEMPLATE = "Hello, %s!";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> get(@QueryParam("name") @DefaultValue("World") String name) {
        return Uni.createFrom().item(new Hello(String.format(TEMPLATE, name)));
    }

    @GET
    @Path("/json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> getJson() {
        return Uni.createFrom().item(new Hello("hello"));
    }

    @GET
    @Path("/foo/{something-with-dash:[A-Z0-9]{4}}")
    public Response doSomething(@PathParam("something-with-dash") String param) {
        return Response.noContent().build();
    }

}
