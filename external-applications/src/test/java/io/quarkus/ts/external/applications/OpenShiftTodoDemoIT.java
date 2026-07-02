package io.quarkus.ts.external.applications;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@DisabledOnNative(reason = "Native + s2i not supported")
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "The newest/3.x SNAPSHOT is not available in public repository.")
@OpenShiftScenario
public class OpenShiftTodoDemoIT extends TodoDemoIT {
}
