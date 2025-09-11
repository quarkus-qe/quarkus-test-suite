package io.quarkus.ts.opentelemetry.reactive.metrics.gc;

import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.StartupEvent;

public class RunGarbageCollectorTrigger {

    void runGarbageCollector(@Observes StartupEvent startupEvent) {
        // required so that we can reliably observe the "jvm.gc.duration" metric
        Runtime.getRuntime().gc();
    }

}
