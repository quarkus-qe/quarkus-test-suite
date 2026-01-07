package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.WebClientOptions;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;
import org.htmlunit.util.NameValuePair;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.nocache.NoCacheFlow;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.LogoutFlow;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.LogoutTenantResolver;

public abstract class AbstractLogoutSinglePageAppFlowIT {

    static final String REALM_DEFAULT = "quarkus";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication(classes = { LogoutFlow.class, LogoutTenantResolver.class, NoCacheFlow.class })
    static RestService app = new RestService()
            .withProperty("keycloak.url", () -> keycloak.getURI(Protocol.HTTPS).toString())
            .withProperties("logout.properties")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void singlePageAppLogoutFlow() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            HtmlPage page = loginToApp(webClient);

            // validate https://github.com/quarkusio/quarkus/pull/50249
            assertEquals("no-store", page.getWebResponse().getResponseHeaderValue("cache-control"),
                    "There should be Cache-control HTTP header with value \"no-store\"");

            assertEquals("alice, cache size: 0", page.getBody().asNormalizedText());
            assertTrue(isCodeFlowCookiePresent(webClient));

            page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow/logout");
            assertThat(page.getBody().asNormalizedText(), containsString("You are logged out"));
            assertFalse(isCodeFlowCookiePresent(webClient));
            // Clear the post logout cookie
            webClient.getCookieManager().clearCookies();
        }
    }

    @Test
    public void RPInitiatedLogoutHeadersTest() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            loginToApp(webClient);

            // we want to test redirect response during the logout flow, not final logout page
            webClient.getOptions().setRedirectEnabled(false);
            WebResponse response = webClient
                    .loadWebResponse(new WebRequest(new URL(app.getURI(Protocol.HTTP).toString() + "/code-flow/logout")));
            assertEquals(302, response.getStatusCode());

            validateClearSiteDataHeader(response);
        }
    }

    @Test
    public void frontChannelLogoutHeadersTest() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            loginToApp(webClient);

            // we want to test redirect response during the logout flow, not final logout page
            webClient.getOptions().setRedirectEnabled(false);
            WebResponse response = webClient
                    .loadWebResponse(
                            new WebRequest(new URL(app.getURI(Protocol.HTTP).toString() + "/code-flow/front-channel-logout")));
            assertEquals(302, response.getStatusCode());

            validateClearSiteDataHeader(response);
        }
    }

    @Test
    public void noCacheControlTest() throws IOException {
        try (final WebClient webClient = createWebClient()) {
            HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/no-cache");

            HtmlForm form = page.getHtmlElementById("kc-form-login");
            form.getInputByName("username").type("alice");
            form.getInputByName("password").type("alice");
            page = form.getButtonByName("login").click();

            // validate https://github.com/quarkusio/quarkus/pull/50249
            assertNull(page.getWebResponse().getResponseHeaderValue("cache-control"),
                    "There should be no Cache-control HTTP header when this option is not enabled");
        }
    }

    private HtmlPage loginToApp(WebClient webClient) throws IOException {
        HtmlPage page = webClient.getPage(app.getURI(Protocol.HTTP).toString() + "/code-flow");

        HtmlForm form = page.getHtmlElementById("kc-form-login");
        form.getInputByName("username").type("alice");
        form.getInputByName("password").type("alice");

        return form.getButtonByName("login").click();
    }

    /**
     * Validate that HTTP response has the ClearSiteData header as specified by "quarkus.oidc.code-flow.logout.clear-site-data"
     * property
     */
    private void validateClearSiteDataHeader(WebResponse logoutResponse) {
        Optional<NameValuePair> clearSiteDataHeader = logoutResponse.getResponseHeaders().stream()
                .filter(pair -> pair.getName().equalsIgnoreCase("clear-site-data"))
                .findFirst();
        assertTrue(clearSiteDataHeader.isPresent(), "There should be Clear-Site-Data header present");

        String header = clearSiteDataHeader.get().getValue();
        List<String> headerValues = Arrays.asList(header.split(","));

        assertTrue(headerValues.contains("\"cache\""),
                "Clear-Site-Data header should contain \"cache\", but was: " + header);
        assertTrue(headerValues.contains("\"cookies\""),
                "Clear-Site-Data header should contain \"cookies\", but was: " + header);
        assertTrue(headerValues.contains("\"storage\""),
                "Clear-Site-Data header should contain \"storage\", but was: " + header);
    }

    private WebClient createWebClient() {
        WebClient webClient = new WebClient();
        WebClientOptions options = webClient.getOptions();
        options.setUseInsecureSSL(true);
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
