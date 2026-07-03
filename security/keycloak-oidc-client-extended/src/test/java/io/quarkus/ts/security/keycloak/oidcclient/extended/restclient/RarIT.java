package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;

@Tag("QUARKUS-7341")
@QuarkusScenario
public class RarIT extends AbstractRarIT {

    @KeycloakContainer(runKeycloakInProdMode = true, command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=oid4vc-vci" })
    static KeycloakService keycloak = new KeycloakService("/rar-realm.json", REALM, DEFAULT_REALM_BASE_PATH);
}
