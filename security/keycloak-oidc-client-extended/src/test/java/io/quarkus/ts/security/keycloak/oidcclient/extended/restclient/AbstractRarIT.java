package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.htmlunit.Page;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7341")
public abstract class AbstractRarIT {

    static final String REALM = "rar";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication(properties = "rar.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    void testConfigBasedRarAuthorizationDetails() throws IOException {
        try (WebClient webClient = createWebClient()) {
            HtmlPage page = webClient.getPage(
                    app.getURI(Protocol.HTTP).withPath("/rar/token-response/authorization-details").toString());

            String redirectUrl = page.getBaseURL().toString();
            assertTrue(redirectUrl.contains("authorization_details"),
                    "Expected authorization_details in redirect URL but was: " + redirectUrl);
            assertTrue(redirectUrl.contains("openid_credential"),
                    "Expected openid_credential in redirect URL but was: " + redirectUrl);

            HtmlForm loginForm = page.getForms().get(0);
            loginForm.getInputByName("username").setValueAttribute("alice");
            loginForm.getInputByName("password").setValueAttribute("alice");

            Page resultPage = loginForm.getButtonByName("login").click();
            String authorizationDetails = resultPage.getWebResponse().getContentAsString().trim();

            assertNotNull(authorizationDetails);
            assertTrue(authorizationDetails.contains("type=openid_credential"),
                    "Expected type=openid_credential but was: " + authorizationDetails);
            assertTrue(authorizationDetails.contains("credential_configuration_id"),
                    "Expected credential_configuration_id but was: " + authorizationDetails);

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
