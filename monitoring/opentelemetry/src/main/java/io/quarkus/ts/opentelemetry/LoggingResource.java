package io.quarkus.ts.opentelemetry;

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
        LOG.info("This is info message");
        LOG.debug("This is a debug message");
        LOG.warn("This is a warning");
        LOG.error("This is an error");
        LOG.trace("Tris is a trace");
        return "This is logging resource";
    }

    @GET
    @Path("/generate/two")
    @Produces(MediaType.TEXT_PLAIN)
    public String generateLogLines() {
        LOG.warn("This is a warning");
        LOG.warn("This is a warning");
        return "Lines generated";
    }

    @GET
    @Path("/generate/twenty")
    @Produces(MediaType.TEXT_PLAIN)
    public String generateTwentyLogLines() {
        for (int i = 0; i < 20; i++) {
            LOG.warn("This is a warning");
        }
        return "Lines generated";
    }
}
