package io.quarkus.ts.vertx;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.jboss.logmanager.MDC;

import io.smallrye.health.SmallRyeHealthReporter;

@Path("/external-health")
@ApplicationScoped
public class ExternalHealthEndpoint {

    public static final String LOGGER_NAME = "test-logger";
    public static final String MDC_KEY = "endpoint.context";
    public static final String MDC_VALUE_PREFIX = "value-from-endpoint-";

    @Inject
    SmallRyeHealthReporter healthReporter;

    @Inject
    InMemoryLogHandler inMemoryLogHandler;

    @GET
    public Response triggerHealthChecks() {
        InMemoryLogHandler.reset();
        MDC.put(MDC_KEY, MDC_VALUE_PREFIX + "ExternalEndpoint");
        // access the MDC context before calling the health reporter to cause the problem
        MDC.clear();
        // call the health reporter
        return Response.ok(healthReporter.getHealth().getPayload()).build();

    }

    @GET
    @Path("/log-records")
    public List<String> logRecords() {
        return inMemoryLogHandler.logRecords();
    }

}