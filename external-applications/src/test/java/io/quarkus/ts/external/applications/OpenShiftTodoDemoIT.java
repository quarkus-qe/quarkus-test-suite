package io.quarkus.ts.external.applications;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "webbundler does not have supported for s390x & ppc64le.")
@DisabledOnNative(reason = "Native + s2i not supported")
@OpenShiftScenario
public class OpenShiftTodoDemoIT extends TodoDemoIT {
}
