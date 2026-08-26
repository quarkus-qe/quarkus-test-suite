package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import org.junit.jupiter.api.BeforeAll;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;

@OpenShiftScenario
public class OpenShiftSpiffeClientAuthIT extends AbstractSpiffeClientAuthIT {

    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}", command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=client-auth-federated,spiffe" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @BeforeAll
    public static void setup() {
        var uri = app.getURI(Protocol.HTTP);
        String bundleUrl = "http://" + uri.getHost() + ":" + uri.getPort() + "/spiffe-test/bundle";
        createSpiffeIdentityProvider(bundleUrl);
    }
}
