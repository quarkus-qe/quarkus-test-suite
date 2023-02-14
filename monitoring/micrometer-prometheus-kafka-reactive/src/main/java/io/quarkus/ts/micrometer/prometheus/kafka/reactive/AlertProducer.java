package io.quarkus.ts.micrometer.prometheus.kafka.reactive;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
public class AlertProducer {

    private static final int SEND_ALERT_EVERY_SECONDS = 5;

    @Outgoing(Channels.CHANNEL_SOURCE_ALERTS)
    public Multi<String> generate() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(SEND_ALERT_EVERY_SECONDS)).map(tick -> "alert" + tick);
    }

}
