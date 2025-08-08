package io.quarkus.ts.security.oidcclient.mtls;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.ts.security.oidcclient.mtls.AbstractOidcMtlsBindingIT.CLIENT_ID;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.function.Executable;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftRhSsoOidcMtlsIT extends KeycloakMtlsAuthN {
    @KeycloakContainer(exposeRawTls = true, image = "${rhbk.image}", command = { "start", "--import-realm",
            "--hostname-strict=false",
            "--features=token-exchange",
            "--https-client-auth=required",
            "--https-key-store-password=password",
            "--https-trust-store-password=password" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, REALM_DEFAULT, DEFAULT_REALM_BASE_PATH)
            // server-keystore.p12 has pre-generated cert with SAN for "keycloak" and "keycloak-secured",
            // which is required for app configuration and trust in the keycloak
            .withProperty("KC_HTTPS_KEY_STORE_FILE",
                    "secret_with_destination::/opt/keycloak/conf/keystore/|server-keystore.p12")
            .withProperty("KC_HTTPS_TRUST_STORE_FILE",
                    "secret_with_destination::/opt/keycloak/conf/truststore/|server-truststore.p12");

    @QuarkusApplication(ssl = true)
    static RestService app = new RestService()
            // App needs to access a valid server hostname in certificate, so we are going in through the service instead of OCP-route
            // because service has consistent name "keycloak-secured" which we can add to pre-generated certs.
            .withProperty("quarkus.oidc.auth-server-url", "https://keycloak-secured:8443/realms/test-mutual-tls-realm")
            // tokens issued to the TS-side client are issued by the full keycloak URL, so we need to verify against that
            .withProperty("quarkus.oidc.token.issuer", keycloak::getRealmUrl)
            // we should not set the CLIENT_SECRET here, as that authentication is handled by mTLS instead
            .withProperty("quarkus.oidc.client-id", CLIENT_ID)
            .withProperty("quarkus.oidc.tls.verification", "required")
            // only p12 stores have configured trust in keycloak-side certs, so we need to use these
            .withProperty("client-ts-file", "client-truststore.p12")
            .withProperty("server-ts-file", "server-truststore.p12")
            .withProperty("client-ks-file", "client-keystore.p12")
            .withProperty("server-ks-file", "server-keystore.p12")
            .withProperty("store-file-extension", P12_KEYSTORE_FILE_TYPE)
            .withProperty("server-store-file-extension", P12_KEYSTORE_FILE_TYPE);

    @Override
    protected String getKeystoreFileExtension() {
        return P12_KEYSTORE_FILE_EXTENSION;
    }

    @Override
    protected KeycloakService getKeycloakService() {
        return keycloak;
    }

    @Override
    protected String getToken(String userName) {
        // We need to override keycloak URI with one with port, see getKeycloakUriWithPort() for more info.
        // Remove this entire overriding method, if the issue is solved.
        return new TokenRequest(getKeycloakUriWithPort().toString(),
                userName, userName, CLIENT_ID)
                .withKeystore(getKeyStorePath())
                .withTrustStore(getTrustStorePath())
                .execAndReturnAccessToken();
    }

    @Override
    protected RestService getApp() {
        return app;
    }

    @Override
    protected String getExpectedMtlsPrincipal() {
        return "CN=localhost,OU=QuarkusQE,O=Redhat,L=Brno,ST=BR,C=CZ";
    }

    @Override
    protected Executable getTokenWithoutMutualTlsHandshake() {
        // We need to inject URI with port, or it will fail on default port missing (see getKeycloakUriWithPort).
        // Remove this entire overriding method, if the issue is solved.
        return () -> new TokenRequest(getKeycloakUriWithPort().toString(), NORMAL_USER, NORMAL_USER).execute();
    }

    /**
     * For whatever crazy reason RestAssured requires to have explicitly defined port 443 in the URI even so it is default one.
     * If the port is not stated the connection will fail with "Unsupported or unrecognized SSL message" exception.
     * FIXME: figure out why this is and fix it.
     */
    private URI getKeycloakUriWithPort() {
        URI keycloakUri = URI.create(keycloak.getRealmUrl());
        // framework sets the port to -1 if default port is used
        if (keycloakUri.getPort() == -1) {
            try {
                return new URI(keycloakUri.getScheme(), keycloakUri.getUserInfo(), keycloakUri.getHost(), 443,
                        keycloakUri.getPath(), null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return keycloakUri;
    }
}
