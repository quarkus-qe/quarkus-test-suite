package io.quarkus.ts.messaging.amqpreactive;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ManualPriceProducer {
    private static final Logger LOG = Logger.getLogger(ManualPriceProducer.class.getName());

    @Channel("generated-price")
    Emitter<Integer> emitter;

    public void send(int value) {
        LOG.info("Price sent: " + value);
        emitter.send(value);
    }
}
