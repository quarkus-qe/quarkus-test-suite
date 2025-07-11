package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.ts.security.oidcclient.mtls.MutualTlsKeycloakService.newKeycloakInstance;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;

@OpenShiftScenario
@Disabled("Needs https://github.com/quarkus-qe/quarkus-test-framework/pull/1527")
public class OpenShiftOidcMtlsBindingIT extends AbstractOidcMtlsBindingIT {

    @KeycloakContainer(command = { "start", "--import-realm", "--hostname-strict=false",
            "--features=token-exchange",
            "--https-client-auth=required",
            "--https-key-store-password=password",
            "--https-trust-store-password=password", "--verbose" }, port = KEYCLOAK_PORT)
    static KeycloakService keycloak = newKeycloakInstance(DEFAULT_REALM_FILE, REALM_DEFAULT, DEFAULT_REALM_BASE_PATH)
            .withProperty("KC_HTTPS_KEY_STORE_FILE",
                    "secret_with_destination::/opt/keycloak/conf/keystore/|server-keystore.p12")
            .withProperty("KC_HTTPS_TRUST_STORE_FILE",
                    "secret_with_destination::/opt/keycloak/conf/truststore/|server-truststore.p12");

}
