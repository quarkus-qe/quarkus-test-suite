package io.quarkus.ts.http.jakartarest.reactive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/execution-model")
public class ExecutionModelResource {
    public static final long SLEEP_MILLIS = 2000;
    public static final String RESPONSE = "hello";

    @GET
    @Path("/imperative")
    public String imperative() throws InterruptedException {
        Thread.sleep(SLEEP_MILLIS);
        return RESPONSE;
    }

    @GET
    @Path("/reactive/uni")
    public Uni<String> reactiveUni() throws InterruptedException {
        Thread.sleep(SLEEP_MILLIS);
        return Uni.createFrom().item(RESPONSE);
    }

    @GET
    @Path("/reactive/multi")
    public Multi<String> reactiveMulti() throws InterruptedException {
        Thread.sleep(SLEEP_MILLIS);
        return Multi.createFrom().item(RESPONSE);
    }

    @GET
    @Path("/reactive/completion-stage")
    public CompletionStage<String> reactiveCompletionStage() throws InterruptedException {
        Thread.sleep(SLEEP_MILLIS);
        return CompletableFuture.completedFuture(RESPONSE);
    }
}
