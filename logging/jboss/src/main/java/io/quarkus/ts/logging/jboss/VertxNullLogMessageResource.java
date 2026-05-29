package io.quarkus.ts.logging.jboss;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

@Path("/vertx-log")
public class VertxNullLogMessageResource {

    private static final Logger VERTX_LOGGER = LoggerFactory.getLogger("vertx-null-message-test");

    @POST
    @Path("/null-message")
    public void logNullMessage() {
        VERTX_LOGGER.error(null);
    }
}
