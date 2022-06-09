package io.quarkus.ts.security.keycloak.authz.reactive;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class KeycloakAuthzSecurityReactiveIT extends BaseAuthzSecurityReactiveIT {

    static final int KEYCLOAK_PORT = 8080;

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @Container(image = "${keycloak.image}", expectedLog = "Http management interface listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT)
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT);

    @Override
    protected KeycloakService getKeycloak() {
        return keycloak;
    }

    @Override
    protected RestService getApp() {
        return app;
    }
}
