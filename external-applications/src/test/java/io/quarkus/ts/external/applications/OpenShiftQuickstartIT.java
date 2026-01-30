package io.quarkus.ts.external.applications;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@Disabled("https://github.com/quarkus-qe/quarkus-test-framework/issues/1817")
@DisabledOnNative(reason = "Native + s2i not supported")
@OpenShiftScenario
public class OpenShiftQuickstartIT extends QuickstartIT {
}
