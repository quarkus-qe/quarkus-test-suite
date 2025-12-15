package io.quarkus.ts.javaee.gettingstarted;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.micrometer.core.annotation.Counted;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)

    @Counted(value = "hello_invocation_count")
    public String hello() {
        return "hello";
    }
}
