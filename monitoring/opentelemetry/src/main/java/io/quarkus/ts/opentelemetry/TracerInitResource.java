package io.quarkus.ts.opentelemetry;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkus.arc.Arc;

@Path("/tracer-init")
public class TracerInitResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        Arc.container().instance(OpenTelemetry.class).get();
        return "Hello from tracer-init";
    }
}
