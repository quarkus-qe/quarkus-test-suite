package io.quarkus.ts.http.advanced.reactive;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

@DisabledOnNative(reason = "Because of https://github.com/quarkusio/quarkus/issues/40533")
@OpenShiftScenario
public class OpenShiftBrotli4JIT extends Brotli4JHttpIT {
}
