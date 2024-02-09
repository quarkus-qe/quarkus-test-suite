package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "keycloak container not available on s390x & ppc64le.")
public class OpenShiftOidcSinglePageAppLogoutFlowIT extends LogoutSinglePageAppFlowIT {
}
