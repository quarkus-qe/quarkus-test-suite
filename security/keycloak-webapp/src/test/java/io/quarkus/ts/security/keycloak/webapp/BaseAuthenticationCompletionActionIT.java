package io.quarkus.ts.security.keycloak.webapp;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlunit.Page;
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

@Tag("QUARKUS-7343")
public abstract class BaseAuthenticationCompletionActionIT {

    private static final String LOGIN_REALM_REGEXP = ".*(Sign|Log) in to " + KeycloakService.DEFAULT_REALM + ".*";

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
    public void verifyActionInvokedOnceWithUserProvisioning() throws Exception {
        resetState();
        loginAs("test-user");

        thenEndpointReturns("/auth-completion/count", "1");
        thenEndpointReturns("/auth-completion/principal", "test-user");

        whenGoTo("/user");
        thenPageReturns("Hello, user test-user");
        thenEndpointReturns("/auth-completion/count", "1");
    }

    @Test
    public void verifyActionInvokedAgainAfterSessionExpiry() throws Exception {
        resetState();
        loginAs("test-user");
        thenEndpointReturns("/auth-completion/count", "1");

        waitForSessionExpiry();
        loginAs("test-user");
        thenEndpointReturns("/auth-completion/count", "2");
    }

    @Test
    public void verifyFailedActionCausesAuthenticationFailure() throws Exception {
        resetState();
        enableFailure();

        whenGoTo("/user");
        thenRedirectToLoginPage();

        webClient.getOptions().setRedirectEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlForm loginForm = ((HtmlPage) page).getForms().get(0);
        loginForm.getInputByName("username").setValue("test-user");
        loginForm.getInputByName("password").setValue("test-user");
        Page loginResponse = loginForm.getButtonByName("login").click();

        assertEquals(302, loginResponse.getWebResponse().getStatusCode());
        String callbackUrl = loginResponse.getWebResponse().getResponseHeaderValue("location");

        WebResponse callbackResponse = webClient.loadWebResponse(
                new WebRequest(URI.create(callbackUrl).toURL()));

        int status = callbackResponse.getStatusCode();
        String location = callbackResponse.getResponseHeaderValue("location");
        boolean redirectToOidcProvider = status == 302 && location != null
                && location.contains("/realms/" + KeycloakService.DEFAULT_REALM);
        assertTrue(redirectToOidcProvider || status >= 400,
                "Authentication should fail when action returns failed Uni. "
                        + "Status: " + status + ", Location: " + location);

        thenCountEquals("/auth-completion/count", 0);
    }

    @Test
    public void verifyMultipleActionsExecuted() throws Exception {
        resetState();
        loginAs("test-user");

        thenEndpointReturns("/auth-completion/count", "1");
        thenEndpointReturns("/auth-completion/secondary-count", "1");
    }

    private void loginAs(String user) throws Exception {
        whenGoTo("/user");
        thenRedirectToLoginPage();

        HtmlForm loginForm = ((HtmlPage) page).getForms().get(0);
        loginForm.getInputByName("username").setValue(user);
        loginForm.getInputByName("password").setValue(user);
        page = loginForm.getButtonByName("login").click();
        thenPageReturns("Hello, user " + user);
    }

    private void waitForSessionExpiry() throws Exception {
        await().atMost(1, TimeUnit.MINUTES).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() -> {
            whenGoTo("/user");
            thenRedirectToLoginPage();
        });
    }

    private void thenEndpointReturns(String path, String expected) throws Exception {
        whenGoTo(path);
        thenPageReturns(expected);
    }

    private void resetState() {
        getApp().given().post("/auth-completion/reset").then().statusCode(200);
    }

    private void enableFailure() {
        getApp().given().post("/auth-completion/enable-fail").then().statusCode(200);
    }

    private void thenCountEquals(String path, int expected) {
        String body = getApp().given().get(path).then().statusCode(200).extract().asString();
        assertEquals(Integer.toString(expected), body);
    }

    private void whenGoTo(String path) throws Exception {
        page = webClient.getPage(appUrl(path));
    }

    private void thenRedirectToLoginPage() {
        assertInstanceOf(HtmlPage.class, page, "Should be in the Login page");
        assertTrue(((HtmlPage) page).getTitleText().matches(LOGIN_REALM_REGEXP),
                "Login page title should display application realm");
    }

    private void thenPageReturns(String expectedMessage) {
        assertInstanceOf(TextPage.class, page, "Should be in a text content page");
        String content = ((TextPage) page).getContent();
        assertTrue(content.startsWith(expectedMessage),
                "Page content should match with " + expectedMessage + " but was: " + content);
    }

    private String appUrl(String path) {
        return getApp().getURI(Protocol.HTTP).withPath(path).toString();
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();
}
