package io.quarkus.ts.messaging.amqpreactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class PriceResource {

    @Inject
    PriceConsumer c;

    @GET
    @Path("/price")
    @Produces(MediaType.TEXT_PLAIN)
    public Response price() {
        return Response.ok().entity(c.getPrices()).build();
    }
}
