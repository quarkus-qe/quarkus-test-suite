package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.TestExecutionProperties;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.LogoutFlow;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.LogoutTenantResolver;

@QuarkusScenario
public class LogoutSinglePageAppFlowIT {

    static final String REALM_DEFAULT = "quarkus";

    @KeycloakContainer(command = {
            "start-dev --import-realm --hostname-strict-https=false --features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService("/kc-logout-realm.json", REALM_DEFAULT, "/realms")
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication(classes = { LogoutFlow.class, LogoutTenantResolver.class })
    static RestService app = new RestService()
            .withProperty("keycloak.url", () -> keycloak.getURI(Protocol.HTTP).toString())
            .withProperty("keycloak.origin", () -> {
                if (TestExecutionProperties.isOpenshiftPlatform()) {
                    // in OpenShift we need scheme + host
                    return keycloak.getURI(Protocol.HTTP).getRestAssuredStyleUri();
                } else {
                    // on Bare Metal we need scheme + host + port
                    return "${keycloak.url}";
                }
            })
            .withProperties("logout.properties");

    @Test
    public void singlePageAppLogoutFlow() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            webClient.getOptions().setRedirectEnabled(true);
            HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow");

            HtmlForm form = page.getHtmlElementById("kc-form-login");
            form.getInputByName("username").type("alice");
            form.getInputByName("password").type("alice");

            page = form.getInputByName("login").click();

            assertEquals("alice, cache size: 0", page.getBody().asNormalizedText());
            assertNotNull(getSessionCookie(webClient, "code-flow"));

            page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow/logout");
            assertThat(page.getBody().asNormalizedText(), containsString("You are logged out"));
            assertNull(getSessionCookie(webClient, "code-flow"));
            // Clear the post logout cookie
            webClient.getCookieManager().clearCookies();
        }
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
