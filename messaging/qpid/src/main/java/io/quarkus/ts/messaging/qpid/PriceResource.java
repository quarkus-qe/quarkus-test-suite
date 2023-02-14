package io.quarkus.ts.messaging.qpid;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * https://quarkus.io/guides/jms#qpid-jms-amqp.
 */
@Path("/")
public class PriceResource {

    @Inject
    PriceConsumer consumer;

    @GET
    @Path("/prices/last")
    @Produces(MediaType.TEXT_PLAIN)
    public String last() {
        return consumer.getLastPrice();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String root() {
        return "All good.";
    }
}
