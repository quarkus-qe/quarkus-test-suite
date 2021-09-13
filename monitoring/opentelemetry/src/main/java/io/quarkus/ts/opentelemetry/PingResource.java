package io.quarkus.ts.opentelemetry;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
