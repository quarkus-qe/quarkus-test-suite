package io.quarkus.ts.stork;

import java.time.Duration;
import java.util.Random;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class KafkaPriceProducer {

    private final Random random = new Random();

    @Outgoing("prices-out")
    public Multi<Double> generate() {
        return Multi.createFrom().ticks().every(Duration.ofMillis(100))
                .map(x -> random.nextDouble());
    }
}
