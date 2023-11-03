package io.quarkus.ts.security.webauthn;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;

@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/1500")
@OpenShiftScenario
public class OpenShiftWebAuthnIT extends MySqlWebAuthnIT {
    // TODO Review the OpenshiftScenario after this issue will be solved: https://github.com/quarkus-qe/quarkus-test-suite/issues/1500
}
