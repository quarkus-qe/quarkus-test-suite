package io.quarkus.ts.monitoring.micrometeropentelemetry.test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.monitoring.micrometeropentelemetry.rest.LoggingResource;

@Tag("https://github.com/quarkusio/quarkus/pull/47458")
@QuarkusScenario
public class DevModeOtelCapabilitiesIT {
    @DevModeQuarkusApplication(classes = LoggingResource.class)
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.traces.enabled", "false")
            .withProperty("quarkus.otel.metrics.enabled", "true")
            .withProperty("quarkus.otel.logs.enabled", "true");

    @Test
    void testShouldNotContainFailedToExport() {
        app.given().get("/logging").then().statusCode(200);
        app.logs().assertDoesNotContain(" Failed to export LogsRequestMarshalers.");
        app.logs().assertDoesNotContain(" Failed to export MetricsRequestMarshalers.");
    }

}
