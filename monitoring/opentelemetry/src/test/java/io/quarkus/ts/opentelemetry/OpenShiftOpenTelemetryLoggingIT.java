package io.quarkus.ts.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
public class OpenShiftOpenTelemetryLoggingIT extends OpenTelemetryLoggingIT {
    @Inject
    static OpenShiftClient ocClient;

    @Override
    protected void validateHostname(LogEntry logEntry) {
        String hostname = ocClient.podsInService(app).get(0).getMetadata().getName();
        assertEquals(hostname, logEntry.hostname(), "Hostname in the logs should be local machine hostname");
    }
}
