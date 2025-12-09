package io.quarkus.ts.security.keycloak.multitenant;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

public abstract class BaseBearerTokenAuthFilterIT {

    private static final String USER = "test-user";
    private WebClient webClient;

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(
            DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.service-tenant.token.require-jwt-introspection-only", "true")
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
    public void testBearerTokenFilterExecutesOnlyForServiceTenant() {

        String accessToken = getAccessToken(Tenant.SERVICE);

        given()
                .auth().oauth2(accessToken)
                .get(appUrl("/user/service-tenant"))
                .then()
                .statusCode(HttpStatus.SC_OK);

        FilterState state = getFilterState();

        assertTrue(state.bearerRequest(),
                "Bearer request filter should execute for service tenant");
        assertTrue(state.bearerResponse(),
                "Bearer response filter should execute for service tenant");
        assertTrue(state.globalFilterCalled(),
                "Global filter (backward compatibility) should execute for service tenant");
        assertTrue(state.combinedFilterCalled(),
                "Combined filter (Bearer + TenantFeature) should execute for service tenant");
    }

    @Test
    public void testBearerTokenFiltersDoNotExecuteForAuthorizationCodeFlow() throws Exception {
        resetFilters();

        HtmlPage loginPage = webClient.getPage(appUrl("/user/webapp-tenant"));
        performLogin(loginPage, USER, USER);

        FilterState state = getFilterState();

        assertFalse(state.bearerRequest(),
                "Bearer request filter should NOT execute for Authorization Code flow");
        assertFalse(state.bearerResponse(),
                "Bearer response filter should NOT execute for Authorization Code flow");
    }

    @Test
    public void testGlobalFilterExecutesForServiceTenant() {
        resetFilters();

        String bearerToken = getAccessToken(Tenant.SERVICE);
        given()
                .auth().oauth2(bearerToken)
                .get(appUrl("/user/service-tenant"))
                .then()
                .statusCode(HttpStatus.SC_OK);

        int count = getGlobalFilterCount();
        assertTrue(count > 0,
                "GlobalCompatibilityFilter should execute for service-tenant (Bearer flow)");
    }

    @Test
    public void testGlobalFilterExecutesMultipleTimes() {
        resetFilters();

        String bearerToken = getAccessToken(Tenant.SERVICE);

        given().auth().oauth2(bearerToken).get(appUrl("/user/service-tenant"))
                .then().statusCode(HttpStatus.SC_OK);
        int countAfterFirst = getGlobalFilterCount();

        given().auth().oauth2(bearerToken).get(appUrl("/user/service-tenant"))
                .then().statusCode(HttpStatus.SC_OK);
        int countAfterSecond = getGlobalFilterCount();

        assertTrue(countAfterFirst > 0,
                "GlobalCompatibilityFilter should execute on first request");
        assertTrue(countAfterSecond > countAfterFirst,
                "GlobalCompatibilityFilter invocation count should increase with each request");
    }

    @Test
    public void testBearerTokenFiltersConsistentExecution() {
        resetFilters();

        String bearerToken = getAccessToken(Tenant.SERVICE);

        given().auth().oauth2(bearerToken).get(appUrl("/user/service-tenant"))
                .then().statusCode(HttpStatus.SC_OK);

        FilterState firstState = getFilterState();
        assertTrue(firstState.bearerRequest(), "Bearer filter should execute on first request");

        given().auth().oauth2(bearerToken).get(appUrl("/user/service-tenant"))
                .then().statusCode(HttpStatus.SC_OK);

        FilterState secondState = getFilterState();
        assertTrue(secondState.bearerRequest(),
                "Bearer filter should remain invoked after second request");
    }

    @Test
    public void testGlobalFilterCapturesTenantId() {
        resetFilters();

        String bearerToken = getAccessToken(Tenant.SERVICE);
        given()
                .auth().oauth2(bearerToken)
                .get(appUrl("/user/service-tenant"))
                .then()
                .statusCode(HttpStatus.SC_OK);

        GlobalFilterState globalState = getGlobalFilterState();

        assertTrue(globalState.invocationCount() > 0,
                "GlobalCompatibilityFilter should have been invoked");
        assertEquals("service-tenant", globalState.lastTenantId(),
                "GlobalCompatibilityFilter should capture correct tenant ID");
    }

    @Test
    public void testMultiTenantFilterExecutesForServiceTenant() {

        String accessToken = getAccessToken(Tenant.SERVICE);

        given()
                .auth().oauth2(accessToken)
                .get(appUrl("/user/service-tenant"))
                .then()
                .statusCode(HttpStatus.SC_OK);

        boolean multiTenantCalled = given()
                .get(appUrl("/oidc-filter-state/multi-tenant"))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(Boolean.class);

        assertTrue(multiTenantCalled,
                "MultiTenantFeatureFilter should execute for 'service-tenant' (it is in the include list)");
    }

    private FilterState getFilterState() {
        return given()
                .get(appUrl("/oidc-filter-state"))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(FilterState.class);
    }

    private GlobalFilterState getGlobalFilterState() {
        return given()
                .get(appUrl("/oidc-filter-state/global"))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(GlobalFilterState.class);
    }

    private int getGlobalFilterCount() {
        return getGlobalFilterState().invocationCount();
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

    record GlobalFilterState(
            int invocationCount,
            String lastTenantId) {
    }
}