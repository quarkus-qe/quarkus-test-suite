package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.s390x.missing.services.excludes", matches = "true", disabledReason = "keycloak container not available on s390x.")
public class OpenShiftOidcRestClientIT extends OidcRestClientIT {
}
