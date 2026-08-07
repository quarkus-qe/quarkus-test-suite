package io.quarkus.ts.opentelemetry.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import io.opentelemetry.api.trace.Tracer;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class TracerInitBean {

    @Inject
    Tracer tracer;

    void startup(@Observes StartupEvent event) {
        tracer.spanBuilder("test").startSpan().end();
    }
}
