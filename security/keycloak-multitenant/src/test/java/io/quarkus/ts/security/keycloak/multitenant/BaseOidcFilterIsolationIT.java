package io.quarkus.ts.security.keycloak.multitenant;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

public abstract class BaseOidcFilterIsolationIT {

    private static final String USER = "test-user";
    private WebClient webClient;

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(
            DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @BeforeEach
    public void setup() {
        resetFilters();
        webClient = new WebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        Logger.getLogger("org.htmlunit.css").setLevel(Level.OFF);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setUseInsecureSSL(true);
    }

    @AfterEach
    public void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    public void testBearerFilterIgnoredDuringAuthorizationCodeFlow() throws Exception {
        HtmlPage loginPage = webClient.getPage(appUrl("/user/webapp-tenant"));
        performLogin(loginPage, USER, USER);

        FilterState state = getFilterState();

        assertFalse(state.bearerRequest(),
                "Bearer filter should be IGNORED during Authorization Code flow");
        assertFalse(state.bearerResponse(),
                "Bearer response filter should be IGNORED during Authorization Code flow");

        assertTrue(state.authorizationCodeFlowRequest(),
                "Authorization Code filter should execute");
    }

    @Test
    public void testAuthorizationCodeFilterIgnoredDuringBearerFlow() {
        String serviceToken = getAccessToken(Tenant.SERVICE);
        given()
                .auth().oauth2(serviceToken)
                .get(appUrl("/user/service-tenant"))
                .then()
                .statusCode(HttpStatus.SC_OK);

        FilterState state = getFilterState();

        assertFalse(state.authorizationCodeFlowRequest(),
                "Authorization Code filter should be IGNORED during Bearer flow");
        assertFalse(state.authorizationCodeFlowResponse(),
                "Authorization Code response filter should be IGNORED during Bearer flow");

        assertTrue(state.bearerRequest(),
                "Bearer filter should execute");
    }

    @Test
    public void testCombinedFilterIgnoredForWebappTenant() throws Exception {
        HtmlPage loginPage = webClient.getPage(appUrl("/user/webapp-tenant"));
        performLogin(loginPage, USER, USER);

        FilterState state = getFilterState();

        assertFalse(state.combinedFilterCalled(),
                "Combined filter (service-tenant) should be IGNORED for webapp-tenant");

        assertTrue(state.globalFilterCalled(),
                "Global filter should execute for any tenant");
    }

    private FilterState getFilterState() {
        return given()
                .get(appUrl("/oidc-filter-state"))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(FilterState.class);
    }

    private void resetFilters() {
        given()
                .post(appUrl("/oidc-filter-state/reset"))
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    private String getAccessToken(Tenant tenant) {
        return keycloak.createAuthzClient(tenant.getClientId(), tenant.getClientSecret())
                .obtainAccessToken(USER, USER)
                .getToken();
    }

    private String appUrl(String path) {
        return app.getURI(Protocol.HTTP).withPath(path).toString();
    }

    private void performLogin(HtmlPage loginPage, String username, String password) throws Exception {
        loginPage.getForms().get(0).getInputByName("username").setValue(username);
        loginPage.getForms().get(0).getInputByName("password").setValue(password);
        loginPage.getForms().get(0).getButtonByName("login").click();
    }

    record FilterState(
            boolean bearerRequest,
            boolean bearerResponse,
            boolean authorizationCodeFlowRequest,
            boolean authorizationCodeFlowResponse,
            boolean combinedFilterCalled,
            boolean globalFilterCalled) {
    }
}