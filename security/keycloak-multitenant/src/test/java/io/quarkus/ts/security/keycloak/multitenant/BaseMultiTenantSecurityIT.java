package io.quarkus.ts.security.keycloak.multitenant;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;

public abstract class BaseMultiTenantSecurityIT {

    protected static final String USER = "test-user";

    private static final String LOGIN_REALM_REGEXP = ".*(Sign|Log) in to " + DEFAULT_REALM + ".*";

    private WebClient webClient;

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

    @ParameterizedTest
    @EnumSource(value = Tenant.class, names = { "WEBAPP", "JWT" })
    public void testLocationParamsForWebAppTenants(Tenant webAppTenant) throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        String loc = webClient.loadWebResponse(new WebRequest(URI.create(getEndpointByTenant(webAppTenant)).toURL()))
                .getResponseHeaderValue("location");
        assertTrue(loc.startsWith(getKeycloak().getURI(Protocol.HTTP).getRestAssuredStyleUri()),
                "Unexpected location for " + getKeycloak().getURI(Protocol.HTTP).getRestAssuredStyleUri() + ". Got: " + loc);
        assertTrue(loc.contains("scope=openid"), "Unexpected scope. Got: " + loc);
        assertTrue(loc.contains("response_type=code"), "Unexpected response type. Got: " + loc);
        assertTrue(loc.contains("client_id=" + webAppTenant.getClientId()),
                "Unexpected client id for " + webAppTenant.getClientId() + " . Got: " + loc);
    }

    @ParameterizedTest
    @EnumSource(value = Tenant.class, names = { "WEBAPP", "JWT" })
    public void testAuthenticationForWebAppTenants(Tenant webAppTenant) throws Exception {
        HtmlPage loginPage = webClient.getPage(getEndpointByTenant(webAppTenant));
        assertTrue(loginPage.getTitleText().matches(LOGIN_REALM_REGEXP),
                "Login page title should display application realm");

        TextPage resourcePage = whenLogin(loginPage, USER);

        assertEndpointMessage(webAppTenant, resourcePage.getContent());
    }

    @Test
    public void testEndpointNeedsAuthenticationForServiceTenant() {
        given().when().get(getEndpointByTenant(Tenant.SERVICE))
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testAuthenticationForServiceTenant() {
        Tenant serviceTenant = Tenant.SERVICE;

        String actualResponse = given()
                .when().auth().oauth2(getAccessToken(serviceTenant))
                .get(getEndpointByTenant(serviceTenant))
                .then().statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEndpointMessage(serviceTenant, actualResponse);
    }

    @Test
    public void testSameTokenShouldBeValidForTenantsUsingSameRealm() throws Exception {
        // When login using WebApp tenant
        HtmlPage loginPage = webClient.getPage(getEndpointByTenant(Tenant.WEBAPP));
        TextPage webAppPage = whenLogin(loginPage, USER);

        assertEndpointMessage(Tenant.WEBAPP, webAppPage.getContent());

        // Then JWT tenant should not request a new login
        TextPage jwtPage = webClient.getPage(getEndpointByTenant(Tenant.JWT));
        assertEndpointMessage(Tenant.JWT, jwtPage.getContent());
    }

    private TextPage whenLogin(HtmlPage loginPage, String user) throws Exception {
        HtmlForm loginForm = loginPage.getForms().get(0);

        loginForm.getInputByName("username").setValue(USER);
        loginForm.getInputByName("password").setValue(USER);
        return loginForm.getInputByName("login").click();
    }

    private void assertEndpointMessage(Tenant tenant, String actualResponse) {
        assertEquals("Hello, user " + USER + " using tenant " + tenant.getValue(), actualResponse);
    }

    private String getAccessToken(Tenant serviceTenant) {
        return getKeycloak().createAuthzClient(serviceTenant.getClientId(), serviceTenant.getClientSecret())
                .obtainAccessToken(USER, USER).getToken();
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();

    private String appUrl(String path) {
        return getApp().getURI(Protocol.HTTP).withPath(path).toString();
    }

    private String getEndpointByTenant(Tenant tenant) {
        return appUrl("/user") + "/" + tenant.getValue();
    }
}
