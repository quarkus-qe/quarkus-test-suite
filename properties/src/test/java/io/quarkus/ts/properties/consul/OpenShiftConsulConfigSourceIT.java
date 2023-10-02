package io.quarkus.ts.properties.consul;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "consul container not available on s390x.")
public class OpenShiftConsulConfigSourceIT extends ConsulConfigSourceIT {
}
