package io.quarkus.ts.monitoring.micrometeropentelemetry.rest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestQuery;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

@Path("/micrometer")
public class MicrometerResource {

    private final Counter counter;
    private final DistributionSummary distributionSummary;

    MicrometerResource(MeterRegistry meterRegistry) {
        counter = Counter.builder("count.me")
                .baseUnit("beans")
                .description("counter used for teasing")
                .tags("region", "test")
                .register(meterRegistry);
        distributionSummary = meterRegistry.summary("my.bytes.received", "protocol", "http");
    }

    @Path("/song")
    @Produces(APPLICATION_JSON)
    @GET
    public Response song(@RestHeader boolean badRequest) {
        return badRequest ? Response.status(400).build() : Response.ok("Ho Hey").build();
    }

    @Path("/counter")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @GET
    public Response counterDouble(@RestQuery Double increase) {
        counter.increment(increase);
        return Response.ok("Counter increased by " + increase).build();
    }

    @Path("/distribution-summary")
    @Produces(APPLICATION_JSON)
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @POST
    public double getMaxBytes(byte[] bytes) {
        distributionSummary.record(bytes.length);
        return distributionSummary.max();
    }

    @Path("/timed")
    @GET
    @Timed("my.timed.method")
    public String helloFromTimedMethod() throws InterruptedException {
        Thread.sleep(1300);
        return "hello from timed method";
    }

    @Counted(value = "counted", recordFailuresOnly = true)
    @Path("/counted-failures")
    @GET
    public String helloFromMethodWithCountedFailures(@RestQuery boolean fail) {
        if (fail) {
            throw new IllegalStateException("expected failure");
        }
        return "hello from counted method";
    }

    @Path("/echo-bytes")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    @POST
    public byte[] echoBytes(byte[] bytes) {
        return bytes;
    }
}
