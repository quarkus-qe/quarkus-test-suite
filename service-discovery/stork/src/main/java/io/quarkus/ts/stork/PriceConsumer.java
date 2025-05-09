package io.quarkus.ts.stork;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
@Path("/price")
public class PriceConsumer {

    private static final Logger LOG = Logger.getLogger(PriceConsumer.class);
    private static final AtomicInteger messageCount = new AtomicInteger(0);

    @Inject
    @RestClient
    IGreetingResource greetingResource;

    @Incoming("prices")
    @RunOnVirtualThread
    @Blocking(ordered = false)
    public void consume(double price) throws InterruptedException {
        LOG.info("consume() - Received price: " + price);

        Thread.sleep(1000);
        try {
            String result = greetingResource.hello();
            LOG.info("Greeting result => " + result);
            messageCount.incrementAndGet();
        } catch (Exception e) {
            LOG.error("Error calling greeting service", e);
        }
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getMessageCount() {
        return String.valueOf(messageCount.get());
    }

    public static int getCount() {
        return messageCount.get();
    }
}
