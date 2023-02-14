package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.HelloResource.NAME;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/hello")
public class HelloAllResource {
    private static final String TEMPLATE = "Hello all, %s!";
    public static final String ALL_ENDPOINT_PATH = "/all";

    @Path(ALL_ENDPOINT_PATH)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Hello> get(@QueryParam(NAME) @DefaultValue("World") String name) {
        return Uni.createFrom().item(new Hello(String.format(TEMPLATE, name)));
    }

}
