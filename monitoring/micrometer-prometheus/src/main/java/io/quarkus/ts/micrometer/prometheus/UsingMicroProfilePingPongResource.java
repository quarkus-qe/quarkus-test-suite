package io.quarkus.ts.micrometer.prometheus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;

@Path("/using-microprofile-pingpong")
public class UsingMicroProfilePingPongResource {

    private static final String PING_PONG = "ping pong";
    private static final long DEFAULT_GAUGE_VALUE = 100L;
    private static long gaugeValue = DEFAULT_GAUGE_VALUE;

    @GET
    @Counted(name = "simple_counter_mp", absolute = true)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/counter")
    public String simpleScenario() {
        return PING_PONG;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/gauges")
    public long gauges(@QueryParam("inc") long inc) {
        gaugeValue += inc;
        return getFirstGauge() + getSecondGauge() + getThirdGauge();
    }

    @Gauge(name = "first_gauge_mp", unit = MetricUnits.NONE)
    public long getFirstGauge() {
        return gaugeValue;
    }

    @Gauge(name = "second_gauge_mp", unit = MetricUnits.NONE)
    public long getSecondGauge() {
        return gaugeValue;
    }

    @Gauge(unit = MetricUnits.NONE)
    public long getThirdGauge() {
        return gaugeValue;
    }
}
