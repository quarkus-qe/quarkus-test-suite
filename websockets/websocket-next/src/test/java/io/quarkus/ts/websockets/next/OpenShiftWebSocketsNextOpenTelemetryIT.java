package io.quarkus.ts.websockets.next;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Tag("QUARKUS-5658")
public class OpenShiftWebSocketsNextOpenTelemetryIT extends WebSocketsNextOpenTelemetryIT {
}
