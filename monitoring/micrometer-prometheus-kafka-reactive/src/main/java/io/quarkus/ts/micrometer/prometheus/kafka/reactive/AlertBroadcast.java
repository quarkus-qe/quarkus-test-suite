package io.quarkus.ts.micrometer.prometheus.kafka.reactive;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Broadcast;

@ApplicationScoped
public class AlertBroadcast {

    @Incoming(Channels.CHANNEL_TARGET_ALERTS)
    @Outgoing(Channels.ALERTS_STREAM)
    @Broadcast
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public Uni<String> broadcast(String alert) {
        return Uni.createFrom().item(alert);
    }

}
