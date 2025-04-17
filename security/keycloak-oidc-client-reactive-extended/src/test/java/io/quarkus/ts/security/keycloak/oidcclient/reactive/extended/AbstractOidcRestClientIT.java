package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.OidcItUtils.createToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.UUID;

import org.apache.http.HttpStatus;
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

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl());

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

}
