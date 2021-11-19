package io.quarkus.ts.http.advanced.reactive;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/hello")
public class HelloResource {
    private static final String TEMPLATE = "Hello, %s!";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> get(@QueryParam("name") @DefaultValue("World") String name) {
        return Uni.createFrom().item(new Hello(String.format(TEMPLATE, name)));
    }
}
