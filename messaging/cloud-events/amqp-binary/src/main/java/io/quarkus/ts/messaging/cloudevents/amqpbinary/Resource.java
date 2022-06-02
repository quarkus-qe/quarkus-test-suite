package io.quarkus.ts.messaging.cloudevents.amqpbinary;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Resource {

    @Inject
    Consumer consumer;

    @GET
    @Path("/result")
    @Produces(MediaType.APPLICATION_JSON)
    public Response result() {
        return Response.ok(consumer.getResults()).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String root() {
        return "All good.";
    }
}
