package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;

@Path("/named-socket")
public class NamedSocketHandlerResource {
    private static final Logger JSON_LOG = Logger.getLogger("named-socket-json-category");
    private static final Logger PLAIN_LOG = Logger.getLogger("named-socket-plain-category");

    @GET
    @Path("/json")
    public void json() {
        JSON_LOG.info("Named Socket Handler JSON");
    }

    @GET
    @Path("/plain")
    public void plain() {
        PLAIN_LOG.info("Named Socket Handler Plain");
    }
}
