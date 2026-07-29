package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;

@Tag("QUARKUS-7341")
@OpenShiftScenario
public class OpenShiftRarIT extends AbstractRarIT {

    // TODO change realm file to rar-realm.json when we move to RHBK 26.7+ (probably 26.8)
    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}", command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=oid4vc-vci" })
    static KeycloakService keycloak = new KeycloakService("/rar-realm-openshift.json", REALM, DEFAULT_REALM_BASE_PATH);
}
