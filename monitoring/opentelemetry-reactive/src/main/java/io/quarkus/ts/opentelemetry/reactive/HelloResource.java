package io.quarkus.ts.opentelemetry.reactive;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.MDC;

import io.smallrye.mutiny.Uni;

@Path("hello")
public class HelloResource {

    @GET
    @Path("/async")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> helloAsync() {
        String responseText = String.format("{\"spanId\": \"%s\", \"traceId\": \"%s\"}", MDC.get("spanId"), MDC.get("traceId"));
        return Uni.createFrom().item(Response.ok(responseText).build());
    }
}
