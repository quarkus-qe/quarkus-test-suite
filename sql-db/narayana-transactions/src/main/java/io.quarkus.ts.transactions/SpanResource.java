package io.quarkus.ts.transactions;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.context.ThreadContext;

import io.opentelemetry.api.trace.Span;
import io.quarkus.bootstrap.forkjoin.QuarkusForkJoinWorkerThreadFactory;

@Path("/span")
public class SpanResource {

    private final ExecutorService myExecutorService;

    private final SpanAsyncService spanAsyncService;

    private final ThreadContext threadContext;

    @Inject
    public SpanResource(ThreadContext threadContext, SpanAsyncService spanAsyncService) {
        this.myExecutorService = createNewExecutor();
        this.spanAsyncService = spanAsyncService;
        this.threadContext = threadContext;
    }

    private static ForkJoinPool createNewExecutor() {
        return new ForkJoinPool(32,
                new QuarkusForkJoinWorkerThreadFactory(), null, true,
                0, 0x7fff, 1, null, 500,
                TimeUnit.MILLISECONDS);
    }

    @Path("/")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> getSpans() {
        return this.threadContext
                .withContextCapture(this.spanAsyncService.runService("Hello " + Span.current().getSpanContext().getTraceId(),
                        this.myExecutorService))
                .thenApplyAsync(message -> message + "-" + Span.current().getSpanContext().getTraceId(),
                        this.myExecutorService);
    }
}
