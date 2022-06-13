package io.quarkus.ts.opentelemetry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class PongResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "pong";
    }
}
