package io.quarkus.ts.http.advanced.reactive;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/hello")
public class HelloResource {

    private static final String TEMPLATE = "Hello, %s!";
    public static final String NAME = "name";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> get(@QueryParam(NAME) @DefaultValue("World") String name) {
        return Uni.createFrom().item(new Hello(String.format(TEMPLATE, name)));
    }
}
