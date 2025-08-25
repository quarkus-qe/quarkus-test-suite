package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class OidcMtlsBindingIT extends AbstractOidcMtlsBindingIT {

    @KeycloakContainer(exposeRawTls = true, command = { "start", "--import-realm", "--hostname-strict=false",
            "--features=token-exchange",
            "--https-client-auth=required",
            "--https-key-store-password=password",
            "--https-trust-store-password=password" }, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, REALM_DEFAULT, DEFAULT_REALM_BASE_PATH)
            .withProperty("KC_HTTPS_KEY_STORE_FILE", "resource_with_destination::/etc/|server-keystore.p12")
            .withProperty("KC_HTTPS_TRUST_STORE_FILE", "resource_with_destination::/etc/|server-truststore.p12");

    @QuarkusApplication(ssl = true, properties = "mtls-binding.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc.client-id", CLIENT_ID);

    @Override
    protected RestService getApp() {
        return app;
    }
}
