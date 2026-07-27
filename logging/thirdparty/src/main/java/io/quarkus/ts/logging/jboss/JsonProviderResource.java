package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@Path("/json-provider")
public class JsonProviderResource {
    private static final Logger LOG = Logger.getLogger("json-provider-category");

    @GET
    @Path("/info")
    public void info() {
        MDC.put("requestId", "request-123");
        LOG.info("JSON Provider Info");
    }

    @GET
    @Path("/error")
    public void error() {
        LOG.error("JSON Provider Error");
    }
}
