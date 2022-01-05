package io.quarkus.ts.micrometer.prometheus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;

@Path("/using-microprofile-pingpong")
public class UsingMicroProfilePingPongResource {

    private static final String PING_PONG = "ping pong";
    private static final long DEFAULT_GAUGE_VALUE = 100L;

    @GET
    @Counted(name = "simple_counter_mp", absolute = true)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/counter")
    public String simpleScenario() {
        return PING_PONG;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/gauge")
    public long highestPrimeNumberSoFar() {
        return getDefaultGauge();
    }

    @Gauge(name = "simple_gauge_mp", unit = MetricUnits.NONE)
    public long getDefaultGauge() {
        return DEFAULT_GAUGE_VALUE;
    }
}
