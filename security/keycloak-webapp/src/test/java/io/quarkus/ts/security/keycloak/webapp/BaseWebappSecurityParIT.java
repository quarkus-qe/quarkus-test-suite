package io.quarkus.ts.security.keycloak.webapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.Page;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;

public abstract class BaseWebappSecurityParIT {

    protected static final String REALM_DEFAULT = "test-realm";
    protected static final String CLIENT_ID_DEFAULT = "test-application-client-par";
    protected static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret-par";

    private static final String REQUEST_URI_VALUE = "urn%3Aietf%3Aparams%3Aoauth%3Arequest_uri";

    private static final String TEST_USER = "test-user";
    private static final String TEST_ADMIN = "test-admin";

    private static final String LOGIN_REALM_REGEXP = ".*(Sign|Log) in to " + REALM_DEFAULT + ".*";

    private WebClient webClient;
    private Page page;

    @BeforeEach
    public void setup() {
        webClient = new WebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        Logger.getLogger("org.htmlunit.css").setLevel(Level.OFF);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setUseInsecureSSL(true);
    }

    @AfterEach
    public void tearDown() {
        Optional.ofNullable(webClient).ifPresent(WebClient::close);
    }

    @Test
    public void verifyLocationHeaders() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        String locationUrl = webClient.loadWebResponse(new WebRequest(URI.create(appUrl("/user")).toURL()))
                .getResponseHeaderValue("location");

        assertTrue(locationUrl.startsWith(getKeycloak().getURI(Protocol.HTTPS).getRestAssuredStyleUri()),
                "Unexpected location for " + getKeycloak().getURI(Protocol.HTTPS).getRestAssuredStyleUri() + ". Got: "
                        + locationUrl);
        assertTrue(locationUrl.contains("client_id=" + CLIENT_ID_DEFAULT), "Unexpected client id. Got: " + locationUrl);
        assertTrue(locationUrl.contains("request_uri=" + REQUEST_URI_VALUE),
                "Unexpected request uri. Got: " + locationUrl);

        // These should not be present with PAR enabled
        assertFalse(locationUrl.contains("scope"), "Url should not contain scope. Got: " + locationUrl);
        assertFalse(locationUrl.contains("response_type"), "Should not contain response_type. Got: " + locationUrl);

        // After tha PAR finish communication check the login page, as Keycloak without PAR set scope and other parameter,
        // but with PAR enabled the Keycloak hide them and use different parameters
        String loginLocationUrl = webClient.loadWebResponse(new WebRequest(URI.create(locationUrl).toURL()))
                .getResponseHeaderValue("location");

        verifyKeycloakParURL(loginLocationUrl);
    }

    @Test
    public void normalUserUserResource() throws Exception {
        goTo("/user");
        redirectToLoginPage();

        loginAs(TEST_USER);
        expectedPageReturnMessage("Hello, user " + TEST_USER);
    }

    @Test
    public void normalUserAdminResource() throws Exception {
        goTo("/admin");
        redirectToLoginPage();

        returnsForbiddenWhenLoginAs(TEST_USER);
    }

    @Test
    public void adminUserUserResource() throws Exception {
        goTo("/user");
        redirectToLoginPage();

        loginAs(TEST_ADMIN);
        expectedPageReturnMessage("Hello, user " + TEST_ADMIN);
    }

    @Test
    public void adminUserAdminResource() throws Exception {
        goTo("/admin");
        redirectToLoginPage();

        loginAs(TEST_ADMIN);
        expectedPageReturnMessage("Hello, admin " + TEST_ADMIN);
    }

    private void loginAs(String user) throws Exception {
        assertInstanceOf(HtmlPage.class, page, "Should be in an HTML page");
        HtmlForm loginForm = ((HtmlPage) page).getForms().get(0);

        loginForm.getInputByName("username").setValue(user);
        loginForm.getInputByName("password").setValue(user);

        page = loginForm.getButtonByName("login").click();
    }

    private void goTo(String path) throws Exception {
        page = webClient.getPage(appUrl(path));
    }

    private void redirectToLoginPage() {
        assertInstanceOf(HtmlPage.class, page, "Should be in the Login page");
        assertTrue(((HtmlPage) page).getTitleText().matches(LOGIN_REALM_REGEXP),
                "Login page title should display application realm");
        verifyKeycloakParURL(page.getUrl().toString());
    }

    private void expectedPageReturnMessage(String expectedMessage) {
        assertInstanceOf(TextPage.class, page, "Should be in a text content page");
        String content = ((TextPage) page).getContent();
        assertTrue(content.startsWith(expectedMessage),
                "Page content should match with " + expectedMessage + " but was: " + content);
    }

    private void returnsForbiddenWhenLoginAs(String user) {
        FailingHttpStatusCodeException exception = assertThrows(FailingHttpStatusCodeException.class,
                () -> loginAs(user), "Should return HTTP status exception");
        assertEquals(HttpStatus.SC_FORBIDDEN, exception.getStatusCode());
    }

    private void verifyKeycloakParURL(String loginLocationUrl) {
        assertTrue(loginLocationUrl.startsWith(getKeycloak().getURI(Protocol.HTTPS).getRestAssuredStyleUri()),
                "Unexpected location for " + getKeycloak().getURI(Protocol.HTTPS).getRestAssuredStyleUri() + ". Got: "
                        + loginLocationUrl);
        assertTrue(loginLocationUrl.contains("client_id=" + CLIENT_ID_DEFAULT),
                "Unexpected client id. Got: " + loginLocationUrl);
        assertTrue(loginLocationUrl.contains("client_data="),
                "Login URL should contain client_data. Got: " + loginLocationUrl);
    }

    protected void goToAndExpectInternalError(String path) {
        FailingHttpStatusCodeException exception = assertThrows(FailingHttpStatusCodeException.class,
                () -> webClient.getPage(appUrl(path)), "Should return internal server error status exception");
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();

    private String appUrl(String path) {
        return getApp().getURI(Protocol.HTTP).withPath(path).toString();
    }
}
