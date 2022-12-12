package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Objects;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.LogoutFlow;

@QuarkusScenario
public class LogoutSinglePageAppFlowIT {

    static final String REALM_DEFAULT = "quarkus";

    @KeycloakContainer(command = {
            "start-dev --import-realm --hostname-strict-https=false --features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService("/kc-logout-realm.json", REALM_DEFAULT, "/realms")
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication(classes = { LogoutFlow.class })
    static RestService app = new RestService()
            .withProperty("keycloak.url", () -> keycloak.getURI(Protocol.HTTP).toString())
            .withProperties("logout.properties");

    @Tag("QUARKUS-2491")
    @Test
    public void singlePageAppLogoutFlow() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            webClient.getOptions().setRedirectEnabled(true);
            String content = makeHttpPostFormLogin(webClient, "/code-flow", "alice", "alice")
                    .getContentAsString();

            assertThat(content, containsString("alice, cache size: 0"));
            assertNotNull(getSessionCookie(webClient, null));

            HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow/logout");
            assertThat(page.getBody().asNormalizedText(), containsString("You are logged out"));
            assertNull(getSessionCookie(webClient, "code-flow"));

            // double-check session is not valid anymore
            boolean isLoginPage = isRedirectedToLoginPage(webClient, "/code-flow/authenticated");
            assertTrue(isLoginPage, "Session is still valid");

            // Clear the post logout cookie
            webClient.getCookieManager().clearCookies();
        }
    }

    private boolean isRedirectedToLoginPage(WebClient webClient, String resourceURL) throws IOException {
        HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + resourceURL);
        return !Objects.isNull(page.getHtmlElementById("kc-form-login"));
    }

    private WebResponse makeHttpPostFormLogin(WebClient webClient, String loginPath, String user, String pwd)
            throws IOException {
        HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + loginPath);
        HtmlForm form = page.getHtmlElementById("kc-form-login");
        form.getInputByName("username").type(user);
        form.getInputByName("password").type(pwd);

        return form.getInputByName("login").click().getWebResponse();
    }

    private WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        return webClient;
    }

    private Cookie getSessionCookie(WebClient webClient, String tenantId) {
        return webClient.getCookieManager().getCookie("q_session" + (tenantId == null ? "" : "_" + tenantId));
    }
}
