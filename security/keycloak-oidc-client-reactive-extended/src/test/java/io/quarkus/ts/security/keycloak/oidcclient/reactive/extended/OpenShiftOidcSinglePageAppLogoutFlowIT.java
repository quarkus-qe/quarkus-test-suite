package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;

@OpenShiftScenario
public class OpenShiftOidcSinglePageAppLogoutFlowIT extends AbstractLogoutSinglePageAppFlowIT {

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false",
            "--features=token-exchange" }, image = "${rhbk.image}")
    static KeycloakService keycloak = new KeycloakService("/quarkus-realm.json", REALM_DEFAULT, DEFAULT_REALM_BASE_PATH);

}
