package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;

@Path("/named-console")
public class NamedConsoleHandlerResource {
    private static final Logger JSON_LOG = Logger.getLogger("named-console-json-category");
    private static final Logger PLAIN_LOG = Logger.getLogger("named-console-plain-category");

    @GET
    @Path("/json")
    public void json() {
        JSON_LOG.info("Named Console Handler JSON");
    }

    @GET
    @Path("/plain")
    public void plain() {
        PLAIN_LOG.info("Named Console Handler Plain");
    }
}
