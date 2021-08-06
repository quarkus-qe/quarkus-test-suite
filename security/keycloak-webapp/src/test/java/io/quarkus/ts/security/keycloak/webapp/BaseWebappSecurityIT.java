package io.quarkus.ts.security.keycloak.webapp;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;

public abstract class BaseWebappSecurityIT {

    static final String REALM_DEFAULT = "test-realm";
    static final String CLIENT_ID_DEFAULT = "test-application-client";

    private WebClient webClient;
    private Page page;

    @BeforeEach
    public void setup() {
        webClient = new WebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.getOptions().setRedirectEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        Optional.ofNullable(webClient).ifPresent(WebClient::close);
    }

    @Test
    public void verifyLocationHeader() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        String loc = webClient.loadWebResponse(new WebRequest(URI.create(appUrl("/user")).toURL()))
                .getResponseHeaderValue("location");

        assertTrue(loc.startsWith(getKeycloak().getHost()),
                "Unexpected location for " + getKeycloak().getHost() + ". Got: " + loc);
        assertTrue(loc.contains("scope=openid"), "Unexpected scope. Got: " + loc);
        assertTrue(loc.contains("response_type=code"), "Unexpected response type. Got: " + loc);
        assertTrue(loc.contains("client_id=test-application-client"), "Unexpected client id. Got: " + loc);
    }

    @Test
    public void normalUserUserResource() throws Exception {
        whenGoTo("/user");
        thenRedirectToLoginPage();

        whenLoginAs("test-user");
        thenPageReturns("Hello, user test-user");
    }

    @Test
    public void normalUserUserResourceIssuer() throws Exception {
        whenGoTo("/user/issuer");
        thenRedirectToLoginPage();

        whenLoginAs("test-user");
        thenPageReturns("user token issued by " + getKeycloak().getHost());
    }

    @Test
    public void normalUserAdminResource() throws Exception {
        whenGoTo("/admin");
        thenRedirectToLoginPage();

        thenReturnsForbiddenWhenLoginAs("test-user");
    }

    @Test
    public void adminUserUserResource() throws Exception {
        whenGoTo("/user");
        thenRedirectToLoginPage();

        whenLoginAs("test-admin");
        thenPageReturns("Hello, user test-admin");
    }

    @Test
    public void adminUserAdminResource() throws Exception {
        whenGoTo("/admin");
        thenRedirectToLoginPage();

        whenLoginAs("test-admin");
        thenPageReturns("Hello, admin test-admin");
    }

    @Test
    public void adminUserAdminResourceIssuer() throws Exception {
        whenGoTo("/admin/issuer");
        thenRedirectToLoginPage();

        whenLoginAs("test-admin");
        thenPageReturns("admin token issued by " + getKeycloak().getHost());
    }

    @Test
    public void sessionExpirationUserResource() throws Exception {
        whenGoTo("/user");
        thenRedirectToLoginPage();
        whenLoginAs("test-user");

        // According to property `quarkus.oidc.token.lifespan-grace` and the property `ssoSessionMaxLifespan`
        // in the keycloak configuration, we need to wait more than 5 seconds for the token expiration.
        await().atMost(1, TimeUnit.MINUTES).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            whenGoTo("/user");
            thenRedirectToLoginPage();
        });
    }

    @Test
    public void sessionRevocationUserResource() throws Exception {
        whenGoTo("/user");
        thenRedirectToLoginPage();
        whenLoginAs("test-user");
        thenPageReturns("Hello, user test-user");
        whenGoTo("/logout");
        whenGoTo("/user");
        thenRedirectToLoginPage();
    }

    private void whenLoginAs(String user) throws Exception {
        assertTrue(page instanceof HtmlPage, "Should be in an HTML page");
        HtmlForm loginForm = ((HtmlPage) page).getForms().get(0);

        loginForm.getInputByName("username").setValueAttribute(user);
        loginForm.getInputByName("password").setValueAttribute(user);

        page = loginForm.getInputByName("login").click();
    }

    private void whenGoTo(String path) throws Exception {
        page = webClient.getPage(appUrl(path));
    }

    private void thenRedirectToLoginPage() {
        assertTrue(page instanceof HtmlPage, "Should be in the Login page");
        assertEquals("Sign in to " + REALM_DEFAULT, ((HtmlPage) page).getTitleText(),
                "Login page title should display application realm");
    }

    private void thenPageReturns(String expectedMessage) {
        assertTrue(page instanceof TextPage, "Should be in a text content page");
        assertTrue(((TextPage) page).getContent().startsWith(expectedMessage),
                "Page content should match with expected content");
    }

    private void thenReturnsForbiddenWhenLoginAs(String user) {
        FailingHttpStatusCodeException exception = assertThrows(FailingHttpStatusCodeException.class,
                () -> whenLoginAs(user), "Should return HTTP status exception");
        assertEquals(HttpStatus.SC_FORBIDDEN, exception.getStatusCode());
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();

    private String appUrl(String path) {
        return getApp().getHost() + ":" + getApp().getPort() + path;
    }
}
