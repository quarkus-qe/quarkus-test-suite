package io.quarkus.ts.hibernate.reactive.http;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/other")
public class OtherResource implements SomeApi {

    @GET
    public Uni<String> doSomething() {
        return Uni.createFrom().item("Hi!");
    }
}
