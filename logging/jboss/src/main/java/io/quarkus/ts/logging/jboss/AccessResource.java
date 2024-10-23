package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Used for access logging test.
 * It serves just as a stable endpoint we can access.
 */
@Path("/access")
public class AccessResource {

    @GET
    public String hello() {
        return "Hello world!";
    }
}
