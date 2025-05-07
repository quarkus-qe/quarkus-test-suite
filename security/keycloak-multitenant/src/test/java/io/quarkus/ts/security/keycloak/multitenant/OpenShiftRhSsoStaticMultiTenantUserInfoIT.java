package io.quarkus.ts.security.keycloak.multitenant;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;

@Tag("QUARKUS-5660")
@OpenShiftScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/1145")
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftRhSsoStaticMultiTenantUserInfoIT extends AbstractStaticMultiTenantUserInfoIT {

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false" }, image = "${rhbk.image}")
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);
}
