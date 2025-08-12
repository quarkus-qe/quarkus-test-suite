package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;

@OpenShiftScenario
public class OpenShiftDpopIT extends AbstractDpopIT {
    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}", command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=dpop" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @Override
    protected String getKeycloakRealmUrl() {
        // RestAssured with openshift KC url need the port otherwise is not able to success TLS handshake
        return keycloak.getRealmUrl().replace("/realms/test-realm", ":443/realms/test-realm");
    }
}
