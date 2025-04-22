package io.quarkus.ts.websockets.next;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Tag("QUARKUS-5667")
public class OpenShiftWebSocketsNextMetricsIT extends WebSocketsNextMetricsIT {
}
