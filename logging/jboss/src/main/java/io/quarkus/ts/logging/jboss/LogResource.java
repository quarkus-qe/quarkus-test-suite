package io.quarkus.ts.logging.jboss;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.jboss.logging.Logger;

import io.quarkus.arc.log.LoggerName;

@Path("/log")
public class LogResource {

    public static final String CUSTOM_CATEGORY = "foo";

    private static final Logger LOG = Logger.getLogger(LogResource.class);

    @Inject
    Logger log;

    @LoggerName(CUSTOM_CATEGORY)
    Logger customCategoryLog;

    @POST
    @Path("/static/{level}")
    public void addLogMessageInStaticLogger(@PathParam("level") String level, @QueryParam("message") String message) {
        addLogMessage(LOG, level, message);
    }

    @POST
    @Path("/field/{level}")
    public void addLogMessageInFieldLogger(@PathParam("level") String level, @QueryParam("message") String message) {
        addLogMessage(log, level, message);
    }

    @POST
    @Path("/field-with-custom-category/{level}")
    public void addLogMessageInFieldWithCustomCategoryLogger(@PathParam("level") String level,
            @QueryParam("message") String message) {
        addLogMessage(customCategoryLog, level, message);
    }

    @GET
    public void logExample() {
        LOG.fatal("Fatal log example");
        LOG.error("Error log example");
        LOG.warn("Warn log example");
        LOG.info("Info log example");
        LOG.debug("Debug log example");
        LOG.trace("Trace log example");

        for (int i = 0; i < 10; i++) {
            LOG.info("Example log message: " + i);
        }
    }

    private void addLogMessage(Logger logger, String level, String message) {
        logger.log(Logger.Level.valueOf(level), message);
    }
}
