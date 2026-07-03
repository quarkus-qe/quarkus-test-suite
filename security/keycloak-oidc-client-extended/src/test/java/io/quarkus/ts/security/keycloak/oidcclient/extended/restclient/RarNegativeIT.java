package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
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

    static final String REALM = "rar";

    @KeycloakContainer(runKeycloakInProdMode = true, command = { "start", "--import-realm",
            "--hostname-strict=false", "--features=oid4vc-vci" })
    static KeycloakService keycloak = new KeycloakService("/rar-realm.json", REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(properties = "rar-wrong-type.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    void testWrongRarTypeAndLocationsProducesError() throws IOException {
        try (WebClient webClient = createWebClient()) {
            HtmlPage page = webClient.getPage(
                    app.getURI(Protocol.HTTP).withPath("/rar-wrong/token-response/authorization-details").toString());

            HtmlForm loginForm = page.getForms().get(0);
            loginForm.getInputByName("username").setValueAttribute("alice");
            loginForm.getInputByName("password").setValueAttribute("alice");

            try {
                loginForm.getButtonByName("login").click();
            } catch (FailingHttpStatusCodeException e) {
                assertEquals(401, e.getStatusCode());
            }

        }
    }

    @Test
    void testWrongRedirectFilterRarTypeProducesError() throws IOException {
        try (WebClient webClient = createWebClient()) {
            HtmlPage page = webClient.getPage(
                    app.getURI(Protocol.HTTP).withPath("/rar-redirect-wrong/token-response/authorization-details")
                            .toString());

            String redirectUrl = page.getBaseURL().toString();
            assertTrue(redirectUrl.contains("authorization_details"),
                    "Expected authorization_details in redirect URL but was: " + redirectUrl);
            assertTrue(redirectUrl.contains("invalid_type"),
                    "Expected invalid_type from redirect filter in URL but was: " + redirectUrl);

            HtmlForm loginForm = page.getForms().get(0);
            loginForm.getInputByName("username").setValueAttribute("alice");
            loginForm.getInputByName("password").setValueAttribute("alice");

            try {
                loginForm.getButtonByName("login").click();
            } catch (FailingHttpStatusCodeException e) {
                assertEquals(401, e.getStatusCode());
            }

        }
    }

    private static WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        return webClient;
    }
}
