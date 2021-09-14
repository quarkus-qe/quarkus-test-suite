package io.quarkus.ts.microprofile;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
