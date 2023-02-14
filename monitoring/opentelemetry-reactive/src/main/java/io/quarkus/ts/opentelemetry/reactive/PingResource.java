package io.quarkus.ts.opentelemetry.reactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

@Path("/ping")
public class PingResource {

    @Inject
    @RestClient
    PingPongService pingPongService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> hello() {
        return Uni.createFrom().item("ping");
    }

    @GET
    @Path("/pong")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callPong() {
        return pingPongService.getPongResponse().onItem().transform(item -> String.format("ping %s", item));
    }
}
