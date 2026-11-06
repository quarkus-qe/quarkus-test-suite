package io.quarkus.ts.messaging.amqpreactive;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/send-prices")
public class GeneratorResource {
    @Inject
    ManualPriceProducer producer;

    @POST
    @Path("/{value}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendPrice(@PathParam("value") int value) {
        producer.send(value);
        return "Sent " + value;
    }
}
