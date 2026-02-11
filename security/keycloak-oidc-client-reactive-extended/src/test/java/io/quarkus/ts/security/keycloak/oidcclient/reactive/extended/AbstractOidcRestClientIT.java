package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.secured.method.MethodSecuredResource.PUBLIC_RESPONSE;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.secured.method.MethodSecuredResource.SECURED_RESPONSE;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.createToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.model.Score;
import io.restassured.http.ContentType;

public abstract class AbstractOidcRestClientIT {

    static final String PING_ENDPOINT = "/%s-ping";
    static final String PONG_ENDPOINT = "/%s-pong";
    static final String WRONG_TOKEN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String PERMISSION_CHECKER_PATH = "/permission-checker";

    private static final String TOKEN_REFRESH_RESPONSE = "token refresh secret response";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties())
            // token introspection is required for revoked access tokens to be actually checked against keycloak
            .withProperty("quarkus.oidc.token.require-jwt-introspection-only", "true")
            .withProperty("quarkus.rest-client-oidc-filter.refresh-on-unauthorized", "true");

    @Test
    public void testRest() {
        assertPingEndpoints("rest");
        assertPongEndpoints("rest");
    }

    @Test
    public void testReactive() {
        assertPingEndpoints("reactive");
        assertPongEndpoints("reactive");
    }

    @Test
    public void testAutoAcquireToken() {
        assertPingEndpoints("auto-acquire-token");
    }

    @Test
    public void testTokenPropagation() {
        assertPingEndpoints("token-propagation");
    }

    @Test
    public void testLookupAuthorization() {
        assertPingEndpoints("rest-lookup-auth");
    }

    @Test
    public void unauthenticatedTest() {
        given().get(PERMISSION_CHECKER_PATH)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void wrongAuthorizationTest() {
        // request should fail if required permission is not met
        given().auth().oauth2(createToken(keycloak))
                .queryParam("projectName", "not-my-project")
                .queryParam("newName", "test-user-new-project")
                .get(PERMISSION_CHECKER_PATH + "/rename")
                .then().statusCode(HttpStatus.SC_FORBIDDEN);

        // request should fail if only one of the required permissions is set
        given().auth().oauth2(createToken(keycloak))
                .queryParam("projectName", "test-user-project")
                .get(PERMISSION_CHECKER_PATH + "/delete")
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void permissionAllowedTest() {
        given().auth().oauth2(createToken(keycloak))
                .queryParam("projectName", "test-user-project")
                .queryParam("newName", "test-user-new-project")
                .get(PERMISSION_CHECKER_PATH + "/rename")
                .then().statusCode(HttpStatus.SC_OK);

        given().auth().oauth2(createToken(keycloak))
                .queryParam("projectName", "test-user-project-deletable")
                .get(PERMISSION_CHECKER_PATH + "/delete")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Tag("QUARKUS-6551")
    public void testRefreshToken() {
        // test default OIDC client with default filter (influenced by config property)
        pingTokenRefreshEndpoints("filter", true);
        // test named OIDC client with default filter (influenced by config property)
        pingTokenRefreshEndpoints("namedFilter", true);
        // test default OIDC client with refreshing enabled using request filter
        pingTokenRefreshEndpoints("refreshEnabled", true);
        // test default OIDC client with refreshing disabled using request filter
        pingTokenRefreshEndpoints("refreshDisabled", false);
    }

    /**
     * Test quarkus REST client, with only some method having authentication set up using @OidcClientFilter.
     */
    @Test
    @Tag("QUARKUS-6971")
    public void testMethodLevelOidcClientFilter() {
        // getting public method without authentication should be no problem
        app.given().get("/method-public/publicNoAuth").then().body(is(PUBLIC_RESPONSE));

        // accessing public endpoint with authentication should cause no problem
        app.given().get("/method-public/publicAuth").then().body(is(PUBLIC_RESPONSE));

        // getting secured method without authentication should get denied
        app.given().get("/method-public/securedNoAuth")
                .then()
                // CustomExceptionMapper is overriding the return code to 500
                // we check body to actually verify that this was 401 Unauthorized
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(containsString("Unauthorized, status code 401"));

        // accessing secured endpoint with authentication should work
        app.given().get("/method-public/securedAuth").then().body(is(SECURED_RESPONSE));
        app.given().get("/method-public/securedNamedAuth").then().body(is(SECURED_RESPONSE));
    }

    private void assertPingEndpoints(String endpointPrefix) {
        String pingEndpoint = String.format(PING_ENDPOINT, endpointPrefix);

        assertPingUnauthorized(pingEndpoint);
        assertPingUnauthorizedWithWrongToken(pingEndpoint);
        assertPingPong(pingEndpoint);
        assertPingPongWithPathParam(pingEndpoint);
        assertPingPongCreate(pingEndpoint);
        assertPingPongUpdate(pingEndpoint);
        assertPingPongDelete(pingEndpoint);
    }

    private void assertPongEndpoints(String endpointPrefix) {
        String pongEndpoint = String.format(PONG_ENDPOINT, endpointPrefix);

        assertPongUnauthorized(pongEndpoint);
        assertPongUnauthorizedWithWrongToken(pongEndpoint);
        assertPongIsAuthorized(pongEndpoint);
        assertNotFoundIsAuthorized(pongEndpoint);
        assertNotFoundUnauthorized(pongEndpoint);
    }

    private void assertPingUnauthorized(String pingEndpoint) {
        given()
                .when().get(pingEndpoint)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private void assertPingUnauthorizedWithWrongToken(String pingEndpoint) {
        given().auth().oauth2(WRONG_TOKEN)
                .when().get(pingEndpoint)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private void assertPongUnauthorized(String pongEndpoint) {
        given()
                .when().get(pongEndpoint)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private void assertPongUnauthorizedWithWrongToken(String pongEndpoint) {
        given().auth().oauth2(WRONG_TOKEN)
                .when().get(pongEndpoint)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private void assertPongIsAuthorized(String pongEndpoint) {
        given().auth().oauth2(createToken(keycloak))
                .when().get(pongEndpoint)
                .then().statusCode(HttpStatus.SC_OK);
    }

    private void assertPingPong(String pingEndpoint) {
        // When calling ping, the rest will invoke also the pong rest endpoint.
        given()
                .auth().oauth2(createToken(keycloak))
                .when().get(pingEndpoint)
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("ping pong"));
    }

    private void assertNotFoundIsAuthorized(String pongEndpoint) {
        given().auth().oauth2(createToken(keycloak))
                .when().get(pongEndpoint + "/notFound/id")
                .then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    private void assertNotFoundUnauthorized(String pongEndpoint) {
        given()
                .when().get(pongEndpoint + "/notFound/id")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private void assertPingPongWithPathParam(String pingEndpoint) {
        final String name = "helloWorld";
        given().auth().oauth2(createToken(keycloak))
                .when()
                .get(pingEndpoint + "/name/" + name)
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping pong " + name));
    }

    private void assertPingPongCreate(String pingEndpoint) {
        Score score = new Score(15, 30);
        given().auth().oauth2(createToken(keycloak))
                .contentType(ContentType.JSON)
                .body(score)
                .when()
                .post(pingEndpoint + "/withBody")
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping -> " + score));
    }

    private void assertPingPongUpdate(String pingEndpoint) {
        Score score = new Score(15, 30);
        given().auth().oauth2(createToken(keycloak))
                .contentType(ContentType.JSON)
                .body(score)
                .when()
                .put(pingEndpoint + "/withBody")
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping -> " + score));
    }

    private void assertPingPongDelete(String pingEndpoint) {
        given().auth().oauth2(createToken(keycloak))
                .contentType(ContentType.JSON)
                .when()
                .delete(pingEndpoint + "/" + UUID.randomUUID())
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping -> true"));
    }

    private void pingTokenRefreshEndpoints(String endpoint, boolean shouldRefresh) {
        // first request should always pass OK
        tokenRefreshExpectSuccess(endpoint);
        // after first request, the token is invalidated, but quarkus is not aware of it
        // so second request should always fail on invalid token
        // actual status code 500 because quarkus-side resource handling the RestClient response will take the authentication failure as server side error
        tokenRefreshExportFailure(endpoint);

        // If token is refreshed then third request should succeed again
        // If not, token will still be invalid
        if (shouldRefresh) {
            tokenRefreshExpectSuccess(endpoint);
        } else {
            tokenRefreshExportFailure(endpoint);
        }
    }

    private void tokenRefreshExpectSuccess(String endpoint) {
        given()
                .get("/token-refresh-public/" + endpoint)
                .then()
                .onFailMessage("Request should succeed for endpoint: " + endpoint)
                .statusCode(200)
                .body(containsString(TOKEN_REFRESH_RESPONSE));
    }

    private void tokenRefreshExportFailure(String endpoint) {
        given()
                .get("/token-refresh-public/" + endpoint)
                .then()
                .onFailMessage("Request should fail for endpoint: " + endpoint)
                .statusCode(500)
                .body(containsString("Unauthorized, status code 401"));
    }

}
