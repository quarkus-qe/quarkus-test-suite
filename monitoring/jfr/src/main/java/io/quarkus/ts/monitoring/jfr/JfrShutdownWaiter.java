package io.quarkus.ts.monitoring.jfr;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;

// TODO: Remove this when https://github.com/quarkusio/quarkus/issues/50392 / https://issues.redhat.com/browse/QUARKUS-6687
// The Quarkus seems to shutdown too fast which causing the Jfr not be properly saved/dumped when Quarkus stops
// For more details see https://github.com/quarkusio/quarkus/issues/50392#issuecomment-3730518384
@ApplicationScoped
public class JfrShutdownWaiter {

    void onStop(@Observes ShutdownEvent ev) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
