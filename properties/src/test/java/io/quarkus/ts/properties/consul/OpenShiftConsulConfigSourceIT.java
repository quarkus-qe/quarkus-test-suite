package io.quarkus.ts.properties.consul;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "Consul for openshift don't have Arm64 image")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "Consul for openshift don't have s390x & ppc64le images")
public class OpenShiftConsulConfigSourceIT extends ConsulConfigSourceIT {
}
