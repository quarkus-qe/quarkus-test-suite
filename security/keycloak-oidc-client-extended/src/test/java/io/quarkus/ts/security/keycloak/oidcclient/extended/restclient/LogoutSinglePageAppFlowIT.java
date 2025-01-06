package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.LogoutFlow;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.LogoutTenantResolver;

@QuarkusScenario
public class LogoutSinglePageAppFlowIT {

    static final String REALM_DEFAULT = "quarkus";

    @KeycloakContainer(command = { "start-dev", "--import-realm" })
    static KeycloakService keycloak = new KeycloakService("/kc-logout-realm.json", REALM_DEFAULT, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(classes = { LogoutFlow.class, LogoutTenantResolver.class })
    static RestService app = new RestService()
            .withProperty("keycloak.url", () -> keycloak.getURI(Protocol.HTTP).toString())
            .withProperties("logout.properties");

    @Test
    public void singlePageAppLogoutFlow() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow");

            HtmlForm form = page.getHtmlElementById("kc-form-login");
            form.getInputByName("username").type("alice");
            form.getInputByName("password").type("alice");

            page = form.getButtonByName("login").click();

            assertEquals("alice, cache size: 0", page.getBody().asNormalizedText());
            assertTrue(isCodeFlowCookiePresent(webClient));

            page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow/logout");
            assertThat(page.getBody().asNormalizedText(), containsString("You are logged out"));
            assertFalse(isCodeFlowCookiePresent(webClient));
            // Clear the post logout cookie
            webClient.getCookieManager().clearCookies();
        }
    }

    private WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        Logger.getLogger("org.htmlunit.css").setLevel(Level.OFF);
        webClient.getOptions().setRedirectEnabled(true);
        return webClient;
    }

    /**
     * Search for "q_session_code-flow" cookie.
     * This might be one cookie, or it might be chunked into several ones,
     * which are named "q_session_code-flow_chunk_1" etc.
     *
     * @return True if cookie is present, chunked or not. False otherwise.
     */
    private boolean isCodeFlowCookiePresent(WebClient webClient) {
        for (Cookie cookie : webClient.getCookieManager().getCookies()) {
            if (cookie.getName().startsWith("q_session_code-flow")) {
                return true;
            }
        }
        return false;
    }
}
