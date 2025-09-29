package io.quarkus.ts.properties.consul;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/hashicorp/consul/issues/21762")
public class OpenShiftConsulConfigSourceIT extends ConsulConfigSourceIT {
}
