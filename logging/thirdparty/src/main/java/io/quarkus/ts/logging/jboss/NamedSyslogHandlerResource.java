package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;

@Path("/named-syslog")
public class NamedSyslogHandlerResource {
    private static final Logger JSON_LOG = Logger.getLogger("named-syslog-json-category");
    private static final Logger PLAIN_LOG = Logger.getLogger("named-syslog-plain-category");

    @GET
    @Path("/json")
    public void json() {
        JSON_LOG.info("Named Syslog Handler JSON");
    }

    @GET
    @Path("/plain")
    public void plain() {
        PLAIN_LOG.info("Named Syslog Handler Plain");
    }
}
