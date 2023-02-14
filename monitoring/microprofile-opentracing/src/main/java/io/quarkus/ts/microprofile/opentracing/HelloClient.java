package io.quarkus.ts.microprofile.opentracing;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "http://microprofile-test:8080/")
public interface HelloClient {
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    @Asynchronous
    CompletionStage<String> get();

    @GET
    @Path("/hello/notFound")
    @Produces(MediaType.TEXT_PLAIN)
    @Asynchronous
    @Fallback(fallbackMethod = "fallback")
    CompletionStage<String> getWithFallback();

    default CompletionStage<String> fallback() {
        return completedFuture("Fallback");
    }
}
