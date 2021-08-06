package io.quarkus.ts.security.keycloak.oidcclient.basic;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class KeycloakOidcClientSecurityIT extends BaseOidcClientSecurityIT {
    static final int KEYCLOAK_PORT = 8080;

    @Container(image = "quay.io/keycloak/keycloak:14.0.0", expectedLog = "Http management interface listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl());

    @Override
    protected KeycloakService getKeycloak() {
        return keycloak;
    }

    @Override
    protected RestService getApp() {
        return app;
    }
}
