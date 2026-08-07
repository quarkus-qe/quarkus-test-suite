package io.quarkus.ts.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.opentelemetry.beans.TracerInitBean;

@Tag("QUARKUS-7957")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
public class OpenTelemetryTracerInitIT {

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = { TracerInitResource.class, TracerInitBean.class })
    static final RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.sdk.disabled", "true")
            .setAutoStart(false);

    @Order(1)
    @Test
    public void testTracerInit() {
        assertDoesNotThrow(app::start, "The app should start without any errors");
    }

    @Order(2)
    @Test
    public void testAppLogsTracerInit() {
        app.logs().assertDoesNotContain("GlobalOpenTelemetry.set has already been called");
    }
}
