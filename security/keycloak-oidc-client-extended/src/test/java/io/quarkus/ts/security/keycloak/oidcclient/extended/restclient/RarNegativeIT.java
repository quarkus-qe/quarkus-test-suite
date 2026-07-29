package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7341")
@QuarkusScenario
public class RarNegativeIT {

    private static final String REALM = "rar";
    private static final String QUARKUS_AUTHORIZATION_CODE_FLOW_FAILURE_LOG = "Authorization code flow has failed, error code: invalid_request, error description: ";

    @KeycloakContainer(runKeycloakInProdMode = true, command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=oid4vc-vci" })
    static KeycloakService keycloak = new KeycloakService("/rar-realm.json", REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(properties = "rar-wrong-type.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    void testWrongRarTypeAndLocationsProducesError() throws IOException {
        // KC 26.7 throw error if the `type` is not supported.
        // Do not test this with KC 26.6 or older as KC failed after the login not immediately
        try (WebClient webClient = createWebClient()) {
            webClient.getPage(
                    app.getURI(Protocol.HTTP).withPath("/rar-wrong/token-response/authorization-details")
                            .toString());
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getStatusCode());
        }

        String keycloakLog = "Unsupported type 'property_invalid_type' of authorization_details parameter supplied in the request";
        keycloak.logs().assertContains(keycloakLog);
        app.logs().assertContains(QUARKUS_AUTHORIZATION_CODE_FLOW_FAILURE_LOG + keycloakLog);
    }

    @Test
    void testWrongRedirectFilterRarTypeProducesError() throws IOException {
        // KC 26.7 throw error if the `type` is not supported.
        // Do not test this with KC 26.6 or older as KC failed after the login not immediately
        try (WebClient webClient = createWebClient()) {
            webClient.getPage(
                    app.getURI(Protocol.HTTP).withPath("/rar-redirect-wrong/token-response/authorization-details")
                            .toString());
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getStatusCode());
        }

        String keycloakLog = "Unsupported type 'added_invalid_type' of authorization_details parameter supplied in the request";
        keycloak.logs().assertContains(keycloakLog);
        app.logs().assertContains(QUARKUS_AUTHORIZATION_CODE_FLOW_FAILURE_LOG + keycloakLog);
    }

    private static WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        return webClient;
    }
}
