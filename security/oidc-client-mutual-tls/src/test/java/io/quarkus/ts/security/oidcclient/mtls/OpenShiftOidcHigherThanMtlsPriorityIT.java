package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftOidcHigherThanMtlsPriorityIT extends AbstractAuthMechanismPriorityIT {

    @KeycloakContainer(exposeRawTls = true, image = "${rhbk.image}", command = {
            "start",
            "--import-realm",
            "--hostname-strict=false",
            "--features=token-exchange",
            "--https-client-auth=required",
            "--https-key-store-password=password",
            "--https-trust-store-password=password" }, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, REALM_DEFAULT, DEFAULT_REALM_BASE_PATH)
            .withProperty("KC_HTTPS_KEY_STORE_FILE",
                    "secret_with_destination::/opt/keycloak/conf/keystore/|server-keystore.p12")
            .withProperty("KC_HTTPS_TRUST_STORE_FILE",
                    "secret_with_destination::/opt/keycloak/conf/truststore/|server-truststore.p12");

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService()
            .withProperties("mtls-oidc-priority.properties")
            .withProperty("quarkus.oidc.auth-server-url", "https://keycloak-secured:8443/realms/test-mutual-tls-realm")
            .withProperty("quarkus.oidc.token.issuer", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", "test-mutual-tls-binding")
            .withProperty("quarkus.http.auth.mtls.priority", "1")
            .withProperty("quarkus.oidc.priority", "2");

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
        return "test-normal-user@gmail.com";
    }
}
