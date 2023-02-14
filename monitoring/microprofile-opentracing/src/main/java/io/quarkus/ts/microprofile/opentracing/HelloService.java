package io.quarkus.ts.microprofile.opentracing;

import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;

import io.opentracing.Tracer;

@ApplicationScoped
public class HelloService {
    @Inject
    ManagedExecutor executor;

    @Inject
    Tracer tracer;

    public CompletionStage<String> get(String name) {
        tracer.activeSpan().log("HelloService called");
        return executor.supplyAsync(() -> {
            tracer.activeSpan().log("HelloService async processing");
            return "Hello, " + name + "!";
        });
    }
}
