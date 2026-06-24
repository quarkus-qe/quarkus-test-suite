package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7342")
@QuarkusScenario
public class MtlsHigherThanOidcPriorityIT extends AbstractAuthMechanismPriorityIT {

    @KeycloakContainer(exposeRawTls = true, command = {
            "start",
            "--import-realm",
            "--hostname-strict=false",
            "--features=token-exchange",
            "--https-client-auth=required",
            "--https-key-store-password=password",
            "--https-trust-store-password=password"
    }, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, REALM_DEFAULT, DEFAULT_REALM_BASE_PATH)
            .withProperty("KC_HTTPS_KEY_STORE_FILE", "resource_with_destination::/etc/|server-keystore.p12")
            .withProperty("KC_HTTPS_TRUST_STORE_FILE", "resource_with_destination::/etc/|server-truststore.p12");

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService()
            .withProperties("mtls-oidc-priority.properties")
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", "test-mutual-tls-binding")
            .withProperty("quarkus.http.auth.mtls.priority", "2")
            .withProperty("quarkus.oidc.priority", "1");

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getExpectedPrincipal() {
        return "CN=localhost,OU=QuarkusQE,O=Redhat,L=Brno,ST=BR,C=CZ";
    }
}
