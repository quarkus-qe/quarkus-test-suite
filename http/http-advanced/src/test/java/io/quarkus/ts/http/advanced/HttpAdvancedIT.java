package io.quarkus.ts.http.advanced;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class HttpAdvancedIT extends BaseHttpAdvancedIT {

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--hostname-strict=false" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH)
            // TODO remove this propery when this issue will be resolved: https://github.com/quarkus-qe/quarkus-test-suite/issues/2106
            .withProperty("JAVA_OPTS_KC_HEAP", "-Xms512m -Xmx1g");

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureHttpServer = true, useTlsRegistry = false))
    static RestService app = new RestService().withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    @Override
    protected RestService getApp() {
        return app;
    }
}
