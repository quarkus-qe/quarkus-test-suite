package io.quarkus.qe.properties.consul;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@OpenShiftScenario
@DisabledOnNative(reason = "TODO: Caused by https://github.com/quarkus-qe/quarkus-test-framework/issues/169")
public class OpenShiftConsulConfigSourceIT extends ConsulConfigSourceIT {
}
