package io.quarkus.ts.properties.consul;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "consul container not available on s390x & ppc64le.")
public class OpenShiftConsulConfigSourceIT extends ConsulConfigSourceIT {
}
