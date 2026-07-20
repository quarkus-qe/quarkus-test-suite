package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;

@Path("/named-file")
public class NamedFileHandlerResource {
    private static final Logger JSON_LOG = Logger.getLogger("named-file-json-category");
    private static final Logger PLAIN_LOG = Logger.getLogger("named-file-plain-category");

    @GET
    @Path("/json")
    public void json() {
        JSON_LOG.info("Named File Handler JSON");
    }

    @GET
    @Path("/plain")
    public void plain() {
        PLAIN_LOG.info("Named File Handler Plain");
    }
}
