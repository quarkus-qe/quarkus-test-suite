package io.quarkus.ts.security.keycloak.webapp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7305")
@QuarkusScenario
public class OidcAbsoluteRedirectIT {

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(classes = { AbsoluteRedirectResource.class }, properties = "absolute-redirect.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", "test-application-client")
            .withProperties(() -> keycloak.getTlsProperties());

    private WebClient webClient;

    @BeforeEach
    public void setup() {
        webClient = new WebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.getOptions().setUseInsecureSSL(true);
        Logger.getLogger("org.htmlunit.css").setLevel(Level.OFF);
    }

    @AfterEach
    public void tearDown() {
        Optional.ofNullable(webClient).ifPresent(WebClient::close);
    }

    @Test
    public void testAbsoluteRedirectPathIsPreserved() throws Exception {
        String callbackUrl = performOidcLogin("/absolute-redirect");

        WebResponse secondRedirect = getRedirectResponse(callbackUrl);
        String location = secondRedirect.getResponseHeaderValue("location");

        assertTrue(location.contains("/absolute-redirect/callback"),
                "Second OIDC redirect must use the absolute redirect URI. Got: " + location);
        assertFalse(location.contains("code="), "Authorization code should be stripped");
        assertFalse(location.contains("state="), "State parameter should be stripped");

        assertAuthenticated(location, "callback: test-user");
    }

    @Test
    public void testAbsoluteRedirectWithRestorePath() throws Exception {
        String callbackUrl = performOidcLogin("/absolute-redirect/restore-path");

        WebResponse secondRedirect = getRedirectResponse(callbackUrl);
        String location = secondRedirect.getResponseHeaderValue("location");

        assertTrue(location.contains("/absolute-redirect/restore-path"),
                "Should restore original request path. Got: " + location);

        assertAuthenticated(location, "restore-path: test-user");
    }

    private String performOidcLogin(String protectedPath) throws Exception {
        webClient.getOptions().setRedirectEnabled(true);
        HtmlPage loginPage = webClient.getPage(appUrl(protectedPath));

        webClient.getOptions().setRedirectEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlForm form = loginPage.getForms().get(0);
        form.getInputByName("username").setValue("test-user");
        form.getInputByName("password").setValue("test-user");
        WebResponse response = form.getButtonByName("login").click().getWebResponse();

        assertEquals(302, response.getStatusCode());
        return response.getResponseHeaderValue("location");
    }

    private WebResponse getRedirectResponse(String url) throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        WebResponse response = webClient.loadWebResponse(new WebRequest(URI.create(url).toURL()));
        assertEquals(302, response.getStatusCode());
        return response;
    }

    private void assertAuthenticated(String location, String expectedContent) throws Exception {
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        TextPage page = assertInstanceOf(TextPage.class, webClient.getPage(location));
        assertTrue(page.getContent().contains(expectedContent),
                "Expected '" + expectedContent + "' but got: " + page.getContent());
    }

    private String appUrl(String path) {
        return app.getURI(Protocol.HTTP).withPath(path).toString();
    }
}
