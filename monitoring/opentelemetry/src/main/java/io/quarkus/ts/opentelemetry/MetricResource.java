package io.quarkus.ts.opentelemetry;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

@Path("/test-metrics")
public class MetricResource {
    private static final Logger LOG = Logger.getLogger(MetricResource.class);
    private final LongCounter counter;

    public MetricResource(Meter meter) {
        this.counter = meter.counterBuilder("test_app_counter")
                .setDescription("test counter for exporter timeout tests")
                .setUnit("invocations")
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String incrementCounter() {
        counter.add(1);
        LOG.info("Test application counter incremented.");
        return "Counter incremented successfully.";
    }
}
