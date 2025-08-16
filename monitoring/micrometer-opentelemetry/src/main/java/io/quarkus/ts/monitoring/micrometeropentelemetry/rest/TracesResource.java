package io.quarkus.ts.monitoring.micrometeropentelemetry.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.opentelemetry.api.trace.Span;

@Path("/traces")
public class TracesResource {

    @Inject
    Span span;

    @Path("/trace-id")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTraceId() {
        return span.getSpanContext().getTraceId();
    }

}
