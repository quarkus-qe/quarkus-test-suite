package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftOidcMtlsBindingIT extends AbstractOidcMtlsBindingIT {

    @KeycloakContainer(exposeRawTls = true, image = "${rhbk.image}", command = { "start", "--import-realm",
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

    @QuarkusApplication(ssl = true, properties = "mtls-binding.properties")
    static RestService app = new RestService()
            // App needs to access a valid server hostname in certificate, so we are going in through the service instead of OCP-route
            // because service has consistent name "keycloak-secured" which we can add to pre-generated certs.
            .withProperty("quarkus.oidc.auth-server-url", "https://keycloak-secured:8443/realms/test-mutual-tls-realm")
            // tokens issued to the TS-side client are issued by the full keycloak URL, so we need to verify against that
            .withProperty("quarkus.oidc.token.issuer", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", CLIENT_ID);

    @Override
    protected RestService getApp() {
        return app;
    }
}
