package io.quarkus.ts.monitoring.jfr;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class HelloResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHello() {
        return "Hello from Quarkus using GET method";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String postHello() {
        return "Hello from Quarkus using POST method";
    }
}
