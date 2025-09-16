package io.quarkus.ts.messaging.kafka.restclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/uppercase")
public class UppercaseRestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String toUppercase(@QueryParam("source") String source) {
        return source.toUpperCase();
    }
}
