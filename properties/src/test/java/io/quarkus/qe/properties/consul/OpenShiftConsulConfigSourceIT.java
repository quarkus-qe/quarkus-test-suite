package io.quarkus.qe.properties.consul;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled
//TODO https://github.com/quarkusio/quarkus/issues/24277
public class OpenShiftConsulConfigSourceIT extends ConsulConfigSourceIT {
}
