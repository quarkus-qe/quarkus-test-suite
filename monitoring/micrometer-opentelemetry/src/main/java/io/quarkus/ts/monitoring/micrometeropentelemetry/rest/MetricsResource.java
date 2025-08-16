package io.quarkus.ts.monitoring.micrometeropentelemetry.rest;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;

@Path("/metrics")
public class MetricsResource {

    private final LongHistogram rolls;

    MetricsResource(Meter meter) {
        rolls = meter.histogramBuilder("hello.roll.dice")
                .ofLongs()
                .setDescription("A distribution of the value of the rolls.")
                .setExplicitBucketBoundariesAdvice(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))
                .setUnit("points")
                .build();
    }

    @Produces(APPLICATION_JSON)
    @GET
    public long hello() {
        long roll = roll();
        rolls.record(roll(), Attributes.of(stringKey("attribute.name"), "value"));
        return roll;
    }

    private long roll() {
        return (long) (Math.random() * 6) + 1;
    }
}
