package io.quarkus.ts.opentelemetry;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

/**
 * Tests that following classes are injectable according to MicroProfile Telemetry Tracing specification:
 * - io.opentelemetry.api.OpenTelemetry
 * - io.opentelemetry.api.trace.Tracer
 * - io.opentelemetry.api.trace.Span
 * - io.opentelemetry.api.baggage.Baggage
 */
@Path("/mp-telemetry-di")
public class MicroProfileTelemetryDIResource {

    public static final int LONG_ATTRIBUTE_LENGTH = 54;
    public static final String LONG_ATTRIBUTE_NAME = "QuarkusQELongAttribute";

    @Inject
    Span span;

    @Inject
    OpenTelemetry openTelemetry;

    @Inject
    Baggage baggage;

    @Inject
    Tracer tracer;

    @GET
    @Path("/span")
    public String getSpanId() {
        span.setAttribute(LONG_ATTRIBUTE_NAME, "a".repeat(LONG_ATTRIBUTE_LENGTH));
        return span.getSpanContext().getSpanId();
    }

    @GET
    @Path("/tracer")
    public String getTracerSpanId() {
        return createSpanAndGetId(tracer);
    }

    @GET
    @Path("/baggage")
    public boolean getBaggage() {
        return baggage.isEmpty();
    }

    @GET
    @Path("/otel")
    public String getOpenTelemetrySpanId() {
        return createSpanAndGetId(openTelemetry.tracerBuilder("myuperdruper").build());
    }

    private static String createSpanAndGetId(Tracer tracer) {
        var nestedSpan = tracer
                .spanBuilder("tracer")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        try (var scope = nestedSpan.makeCurrent()) {
            nestedSpan.end();
            return nestedSpan.getSpanContext().getSpanId();
        }
    }
}
