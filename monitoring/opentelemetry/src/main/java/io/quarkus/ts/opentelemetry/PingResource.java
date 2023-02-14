package io.quarkus.ts.opentelemetry;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/ping")
public class PingResource {

    @Inject
    @RestClient
    PingPongService pingPongService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "ping";
    }

    @GET
    @Path("/pong")
    @Produces(MediaType.TEXT_PLAIN)
    public String callPong() {
        return "ping " + pingPongService.getPongResponse();
    }
}
