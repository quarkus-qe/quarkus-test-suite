package io.quarkus.ts.external.applications;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@DisabledOnNative(reason = "Native + s2i not supported")
@OpenShiftScenario
@DisabledOnAarch64Snapshot(reason = "S2I external repo not supported on aarch OCP yet")
public class OpenShiftQuickstartIT extends QuickstartIT {
}
