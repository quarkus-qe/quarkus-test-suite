package io.quarkus.ts.messaging.qpid;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
