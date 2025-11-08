package io.quarkus.ts.security.keycloak.oauth2;

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
public class KeycloakOauth2SecurityIT extends BaseOauth2SecurityIT {

    @KeycloakContainer(runKeycloakInProdMode = true, certificateFormat = Certificate.Format.PEM)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oauth2.introspection-url",
                    () -> keycloak.getRealmUrl() + "/protocol/openid-connect/token/introspect")
            .withProperties(() -> keycloak.getTlsProperties())
            // The Oauth2 not supporting the TLS registry so this needs to be set
            .withProperty("quarkus.oauth2.ca-cert-file", "${quarkus.tls.keycloak.trust-store.pem.certs}");

    @Override
    protected KeycloakService getKeycloak() {
        return keycloak;
    }

    @Override
    protected RestService getApp() {
        return app;
    }
}
