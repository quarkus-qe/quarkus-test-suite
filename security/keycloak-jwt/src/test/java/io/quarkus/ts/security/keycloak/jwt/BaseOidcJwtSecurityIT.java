package io.quarkus.ts.security.keycloak.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;

public abstract class BaseOidcJwtSecurityIT {

    static final String REALM_DEFAULT = "test-realm";
    static final String CLIENT_ID_DEFAULT = "test-application-client";

    private WebClient webClient;
    private HtmlPage page;

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
    public void securedAsAdminEveryoneEndpoint() throws Exception {
        whenGoTo("/secured/everyone");
        thenRedirectToLoginPage();

        whenLoginAs("test-admin");
        thenPageReturns("Hello, test-admin, your token was issued by " + getKeycloak().getHost());
    }

    @Test
    public void securedAsAdminAdminEndpoint() throws Exception {
        whenGoTo("/secured/admin");
        thenRedirectToLoginPage();

        whenLoginAs("test-admin");
        thenPageReturns("Restricted area! Admin access granted!");
    }

    @Test
    public void securedAsUserEveryoneEndpoint() throws Exception {
        whenGoTo("/secured/everyone");
        thenRedirectToLoginPage();

        whenLoginAs("test-user");
        thenPageReturns("Hello, test-user, your token was issued by " + getKeycloak().getHost());
    }

    @Test
    public void securedAsUserAdminEndpoint() throws Exception {
        whenGoTo("/secured/admin");
        thenRedirectToLoginPage();

        thenReturnsForbiddenWhenLoginAs("test-user");
    }

    private void whenLoginAs(String user) throws Exception {
        HtmlForm loginForm = page.getForms().get(0);

        loginForm.getInputByName("username").setValueAttribute(user);
        loginForm.getInputByName("password").setValueAttribute(user);

        page = loginForm.getInputByName("login").click();
    }

    private void whenGoTo(String path) throws Exception {
        page = webClient.getPage(getApp().getHost() + ":" + getApp().getPort() + path);
    }

    private void thenRedirectToLoginPage() {
        assertEquals("Sign in to " + REALM_DEFAULT, page.getTitleText(),
                "Login page title should display application realm");
    }

    private void thenPageReturns(String expectedMessage) {
        assertTrue(page.asText().startsWith(expectedMessage), "Page content should match with expected content");
    }

    private void thenReturnsForbiddenWhenLoginAs(String user) {
        FailingHttpStatusCodeException exception = assertThrows(FailingHttpStatusCodeException.class,
                () -> whenLoginAs(user), "Should return HTTP status exception");
        assertEquals(HttpStatus.SC_FORBIDDEN, exception.getStatusCode());
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();
}
