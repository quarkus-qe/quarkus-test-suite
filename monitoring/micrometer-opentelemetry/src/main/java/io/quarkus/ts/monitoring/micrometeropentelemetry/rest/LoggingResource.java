package io.quarkus.ts.monitoring.micrometeropentelemetry.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

@Path("/logging")
public class LoggingResource {

    private static final Logger LOG = Logger.getLogger(LoggingResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOG.info("This is info message 1");
        LOG.info("This is info message 2");
        LOG.debug("This is a debug message 1");
        LOG.debug("This is a debug message 2");
        LOG.warn("This is a warning 1");
        LOG.warn("This is a warning 2");
        LOG.error("This is an error 1");
        LOG.error("This is an error 2");
        LOG.trace("Tris is a trace 1");
        LOG.trace("Tris is a trace 2");
        return "This is logging resource";
    }

}