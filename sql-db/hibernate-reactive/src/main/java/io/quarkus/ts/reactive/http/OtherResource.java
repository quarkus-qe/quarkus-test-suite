package io.quarkus.ts.reactive.http;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
