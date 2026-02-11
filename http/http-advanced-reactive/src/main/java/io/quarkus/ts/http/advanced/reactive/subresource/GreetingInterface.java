package io.quarkus.ts.http.advanced.reactive.subresource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public interface GreetingInterface {

    @GET
    @Path("/interface-greeting")
    String interfaceGreeting();
}
