package io.quarkus.ts.messaging.cloudevents.amqpjson;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
