package io.quarkus.ts.security.keycloak.multitenant;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;

@Tag("QUARKUS-5660")
@QuarkusScenario
public class KeycloakStaticMultiTenantUserInfoIT extends AbstractStaticMultiTenantUserInfoIT {

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);
}
