package io.quarkus.ts.logging.jboss;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

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
    }

    private void addLogMessage(Logger logger, String level, String message) {
        logger.log(Logger.Level.valueOf(level), message);
    }
}
