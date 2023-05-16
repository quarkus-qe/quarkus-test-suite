package io.quarkus.ts.transactions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.context.ThreadContext;

import io.opentelemetry.api.trace.Span;

@ApplicationScoped
public class SpanAsyncService {

    private final ThreadContext threadContext;

    public SpanAsyncService(ThreadContext threadContext) {
        this.threadContext = threadContext;
    }

    public CompletionStage<String> runService(String greeting, ExecutorService executorService) {
        return this.threadContext.withContextCapture(CompletableFuture.supplyAsync(() -> greeting, executorService))
                .thenApplyAsync(message -> message + "-" + Span.current().getSpanContext().getTraceId(), executorService);
    }

}
