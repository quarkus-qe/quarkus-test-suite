package io.quarkus.ts.security;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftRhSso74OidcSecurityIT extends BaseOidcSecurityIT {

    static final int KEYCLOAK_PORT = 8080;

    @Container(image = "registry.redhat.io/rh-sso-7/sso74-openshift-rhel8", expectedLog = "Http management interface listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService(REALM_DEFAULT)
            .withProperty("SSO_IMPORT_FILE", "resource::/keycloak-realm.json");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT);

    @Override
    protected KeycloakService getKeycloak() {
        return keycloak;
    }
}
