package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class OidcRequestResponseCustomizationIT {

    private static WireMockServer wiremock;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url",
                    () -> getWiremock().baseUrl() + "/auth/realms/test-realm")
            .withProperty("quarkus.oidc.client-id", "test-client")
            .withProperty("quarkus.oidc.credentials.secret", "test-secret")
            .withProperty("quarkus.oidc-client-registration.discovery-enabled", "false")
            .withProperty("quarkus.oidc-client-registration.registration-path", "/clients-registrations/default")
            .withProperty("quarkus.oidc-client-registration.auth-server-url",
                    () -> getWiremock().baseUrl() + "/auth/realms/test-realm")
            .withProperty("quarkus.http.auth.permission.all.policy", "permit");

    private static WireMockServer getWiremock() {
        if (wiremock == null) {
            wiremock = new WireMockServer(options().dynamicPort());
            wiremock.start();
            OidcWiremockTestUtils.setupOidc(wiremock);
        }
        return wiremock;
    }

    @BeforeEach
    public void setupTest() {
        wiremock.resetRequests();
        clearFilterLogs();
    }

    @AfterAll
    static void afterAll() {
        wiremock.stop();
    }

    private static void clearFilterLogs() {
        app.given().delete("/filter-customization-messages/clear");
    }

    private static String createTokenResponse(String scope) {
        return String.format("""
                {
                    "access_token": "mock-access-token-%s",
                    "token_type": "Bearer",
                    "expires_in": 300,
                    "scope": "%s"
                }
                """, System.currentTimeMillis(), scope);
    }

    private static String createTokenResponseWithRefresh(String scope) {
        return String.format("""
                {
                    "access_token": "mock-access-token-%s",
                    "token_type": "Bearer",
                    "expires_in": 300,
                    "refresh_token": "mock-refresh-token",
                    "scope": "%s"
                }
                """, System.currentTimeMillis(), scope);
    }

    @Test
    public void testProviderReceivesModifiedRequestBody() {
        mockTokenEndpoint("client_credentials", "openid profile", false);

        Response response = requestToken();
        Assertions.assertNotNull(response.asString());
        Assertions.assertFalse(response.asString().isEmpty());

        verifyFilterInvoked("request", "Custom param added", "token request");
        verifyWireMockRequest("client_credentials");
    }

    @Test
    public void testClientReceivesModifiedResponseBody() {
        mockTokenEndpoint("client_credentials", "openid,profile,email", false);
        requestToken();

        verifyFilterInvoked("response",
                "Scope corrected from 'openid,profile,email' to 'openid profile email'",
                "response transformation");
    }

    @Test
    public void testProviderReceivesChainedRequestBody() {
        wiremock.stubFor(
                post(urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
                        .withRequestBody(containing("custom_param=custom_value"))
                        .withRequestBody(containing("chained_param=added_by_second_filter"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(createTokenResponse("openid profile"))));

        Response response = requestToken();
        Assertions.assertNotNull(response.asString());

        verifyFilterInvoked("request", "Custom param added", "first filter");
        verifyFilterInvoked("chained-request", "Second filter acted and added chained_param", "second filter");
    }

    @Test
    public void testTokenRefreshTriggersFilters() {
        mockTokenEndpoint("client_credentials", "openid profile", true);
        requestToken();
        verifyFilterInvoked("request", "Custom param added", "initial token request");

        clearFilterLogs();

        mockTokenEndpoint("refresh_token", "openid,profile", true);
        Response refreshResponse = refreshToken();
        Assertions.assertNotNull(refreshResponse.asString());

        verifyFilterInvoked("request", "Custom param added", "token refresh");
        verifyFilterInvoked("response", "Response body intercepted", "token refresh");
        verifyFilterInvoked("response", "Scope corrected", "scope transformation");
        verifyWireMockRequest("refresh_token");
    }

    @Test
    public void testFilterHandlesMalformedProviderResponse() {
        wiremock.stubFor(
                post(urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody("{ invalid json ]")));

        app.given()
                .get("/generate-token/client-credentials")
                .then()
                .statusCode(500);
    }

    @Test
    public void testProviderRequiresCustomParameter() {

        wiremock.stubFor(
                post(urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
                        .withRequestBody(notMatching(".*custom_param=custom_value.*"))
                        .willReturn(aResponse()
                                .withStatus(400)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody("{\"error\":\"invalid_request\",\"error_description\":\"Missing custom_param\"}")));

        mockTokenEndpoint("client_credentials", "openid profile", false);

        Response response = requestToken();
        Assertions.assertNotNull(response.asString());

        verifyFilterInvoked("request", "Custom param added", "provider validation");
    }

    @Test
    public void testClientRegistrationRequestFilterModifiesBody() {
        wiremock.stubFor(
                post(urlEqualTo("/auth/realms/test-realm/clients-registrations/default"))
                        .withRequestBody(containing("\"client_name\":\"Modified_TestClient\""))
                        .willReturn(aResponse()
                                .withStatus(201)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(
                                        "{\"client_id\":\"test-client-123\",\"client_secret\":\"secret-123\",\"client_name\":\"Modified_TestClient\",\"redirect_uris\":[\"http://localhost:8080/callback\"]}")));

        String requestBody = "{\"clientName\": \"TestClient\"}";

        app.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/client-registration/register")
                .then()
                .statusCode(200);

        app.given()
                .get("/filter-customization-messages/client-registration-request")
                .then()
                .statusCode(200)
                .body("$", hasItem(containsString("Modified client_name from 'TestClient' to 'Modified_TestClient'")));

        wiremock.verify(1,
                WireMock.postRequestedFor(
                        urlEqualTo("/auth/realms/test-realm/clients-registrations/default"))
                        .withRequestBody(containing("Modified_TestClient")));
    }

    @Test
    public void testClientRegistrationResponseFilterAddsMetadata() {
        wiremock.stubFor(
                post(urlEqualTo("/auth/realms/test-realm/clients-registrations/default"))
                        .willReturn(aResponse()
                                .withStatus(201)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(
                                        "{\"client_id\":\"test-client-456\",\"client_secret\":\"secret-456\",\"client_name\":\"TestClient\",\"redirect_uris\":[\"http://localhost:8080/callback\"]}")));

        String requestBody = "{\"clientName\": \"TestClient\"}";

        app.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/client-registration/register")
                .then()
                .statusCode(200)
                .body("client_id", equalTo("test-client-456"))
                .body("custom_metadata", equalTo("Added by filter"));

        app.given()
                .get("/filter-customization-messages/client-registration-response")
                .then()
                .statusCode(200)
                .body("$", hasItem(containsString("Added custom_metadata to response")));
    }

    private void mockTokenEndpoint(String grantType, String scope, boolean withRefreshToken) {
        String body = withRefreshToken
                ? createTokenResponseWithRefresh(scope)
                : createTokenResponse(scope);

        wiremock.stubFor(
                post(urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
                        .withRequestBody(containing("grant_type=" + grantType))
                        .withRequestBody(containing("custom_param=custom_value"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(body)));
    }

    private Response requestToken() {
        return app.given()
                .get("/generate-token/client-credentials")
                .then()
                .statusCode(200)
                .extract().response();
    }

    private Response refreshToken() {
        return app.given()
                .queryParam("refreshToken", "mock-refresh-token")
                .get("/token/refresh")
                .then()
                .statusCode(200)
                .extract().response();
    }

    private void verifyFilterInvoked(String filterType, String expectedContent, String context) {
        Response logs = app.given()
                .get("/filter-customization-messages/" + filterType)
                .then()
                .statusCode(200)
                .extract().response();

        Assertions.assertTrue(logs.asString().contains(expectedContent),
                String.format("%s filter should contain '%s' during %s",
                        filterType, expectedContent, context));
    }

    private void verifyWireMockRequest(String grantType) {
        wiremock.verify(1,
                WireMock.postRequestedFor(
                        urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/token"))
                        .withRequestBody(containing("grant_type=" + grantType))
                        .withRequestBody(containing("custom_param=custom_value")));
    }

}
