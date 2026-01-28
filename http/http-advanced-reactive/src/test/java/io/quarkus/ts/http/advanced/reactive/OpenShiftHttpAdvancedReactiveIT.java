package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftHttpAdvancedReactiveIT extends BaseHttpAdvancedReactiveIT {

    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}")
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureHttpServer = true, useTlsRegistry = false))
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperties(keycloak::getTlsProperties);

    @Override
    protected RestService getApp() {
        return app;
    }
}
