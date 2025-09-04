package io.quarkus.ts.monitoring.micrometeropentelemetry.test;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "grafana/otel-lgtm container not available on s390x & ppc64le.")
public class OpenShiftMicrometerOpenTelemetryBridgeIT extends MicrometerOpenTelemetryBridgeIT {

}
