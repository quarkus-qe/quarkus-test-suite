package io.quarkus.ts.microprofile;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.opentracing.Tracer;

@Path("/client")
public class ClientResource {
    @Inject
    @RestClient
    HelloClient hello;

    @Inject
    Tracer tracer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> get() {
        tracer.activeSpan().log("ClientResource called");
        return hello.get().thenApply(result -> "Client got: " + result);
    }

    @GET
    @Path("/fallback")
    @Produces(MediaType.TEXT_PLAIN)
    @Asynchronous
    @Retry
    public CompletionStage<String> getWithFallback() {
        tracer.activeSpan().log("ClientResource called");
        return hello.getWithFallback().thenApply(result -> "Client got: " + result);
    }
}
