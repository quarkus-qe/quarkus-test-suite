package io.quarkus.ts.microprofile.opentracing;

import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Retry;

import io.opentracing.Tracer;

@Path("/hello")
@Produces(MediaType.TEXT_PLAIN)
public class HelloResource {
    @Inject
    HelloService hello;

    @Inject
    Tracer tracer;

    @GET
    @Asynchronous
    @Retry
    public CompletionStage<String> get(@QueryParam("name") @DefaultValue("World") String name) {
        tracer.activeSpan().log("HelloResource called");
        return hello.get(name);
    }
}
