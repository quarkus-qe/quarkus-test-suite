package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;

@QuarkusScenario
public class LogoutSinglePageAppFlowIT extends AbstractLogoutSinglePageAppFlowIT {

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false", "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService("/quarkus-realm.json", REALM_DEFAULT, DEFAULT_REALM_BASE_PATH);

}
