package io.quarkus.ts.security.keycloak;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.security.keycloak.BaseOidcSecurityIT.CLIENT_ID_DEFAULT;
import static io.quarkus.ts.security.keycloak.BaseOidcSecurityIT.CLIENT_SECRET_DEFAULT;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfSystemProperty(named = "ts.redhat.registry.enabled", matches = "true")
public class OpenShiftMetaEndpointIT extends MetaEndpointIT {

    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}")
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService http = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.resource-metadata.enabled", "true")
            .withProperty("quarkus.oidc.client-id", BaseOidcSecurityIT.CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", BaseOidcSecurityIT.CLIENT_SECRET_DEFAULT)
            .withProperties(keycloak::getTlsProperties);

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureTruststore = true, configureHttpServer = true, useTlsRegistry = false, servingCertificates = @Certificate.ServingCertificates(addServiceCertificate = true)))
    static RestService https = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.resource-metadata.enabled", "true")
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.oidc.resource-metadata.resource", CUSTOM_ENDPOINT)
            .withProperty("quarkus.oidc.client-id", BaseOidcSecurityIT.CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", BaseOidcSecurityIT.CLIENT_SECRET_DEFAULT)
            .withProperties(keycloak::getTlsProperties);

    @Override
    protected String getKeycloakRealmUrl() {
        // RestAssured with openshift KC url need the port otherwise is not able to success TLS handshake
        return keycloak.getRealmUrl().replace("/realms", ":443/realms");
    }

    @Override
    RestService getHttp() {
        return super.getHttp();
    }

    @Override
    RestService getHttps() {
        return super.getHttps();
    }
}
