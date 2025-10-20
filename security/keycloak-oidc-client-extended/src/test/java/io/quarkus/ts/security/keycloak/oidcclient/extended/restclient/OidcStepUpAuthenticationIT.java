package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;

@Tag("QUARKUS-6260")
@QuarkusScenario
public class OidcStepUpAuthenticationIT extends AbstractOidcStepUpAuthenticationIT {

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService("/test-realm.json", "test", DEFAULT_REALM_BASE_PATH);

}
