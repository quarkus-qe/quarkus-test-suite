package io.quarkus.ts.security.keycloak;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Tag("QUARKUS-5617")
public class OpenShiftMultiMechanismAuthenticationIT extends MultiMechanismAuthenticationIT {
}
