package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.model.Score;
import io.restassured.http.ContentType;

@QuarkusScenario
public class OidcRestClientIT extends BaseOidcIT {

    static final String PING_ENDPOINT = "/%s-ping";
    static final String PONG_ENDPOINT = "/%s-pong";
    static final String WRONG_TOKEN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

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
    @DisabledOnNative(reason = "Annotation @ClientHeaderParam not working in Native. Reported by https://github.com/quarkusio/quarkus/issues/13660")
    public void testLookupAuthorization() {
        assertPingEndpoints("rest-lookup-auth");
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
        given().auth().oauth2(createToken())
                .when().get(pongEndpoint)
                .then().statusCode(HttpStatus.SC_OK);
    }

    private void assertPingPong(String pingEndpoint) {
        // When calling ping, the rest will invoke also the pong rest endpoint.
        given()
                .auth().oauth2(createToken())
                .when().get(pingEndpoint)
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("ping pong"));
    }

    private void assertNotFoundIsAuthorized(String pongEndpoint) {
        given().auth().oauth2(createToken())
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
        given().auth().oauth2(createToken())
                .when()
                .get(pingEndpoint + "/name/" + name)
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping pong " + name));
    }

    private void assertPingPongCreate(String pingEndpoint) {
        Score score = new Score(15, 30);
        given().auth().oauth2(createToken())
                .contentType(ContentType.JSON)
                .body(score)
                .when()
                .post(pingEndpoint + "/withBody")
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping -> " + score.toString()));
    }

    private void assertPingPongUpdate(String pingEndpoint) {
        Score score = new Score(15, 30);
        given().auth().oauth2(createToken())
                .contentType(ContentType.JSON)
                .body(score)
                .when()
                .put(pingEndpoint + "/withBody")
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping -> " + score.toString()));
    }

    private void assertPingPongDelete(String pingEndpoint) {
        given().auth().oauth2(createToken())
                .contentType(ContentType.JSON)
                .when()
                .delete(pingEndpoint + "/" + UUID.randomUUID().toString())
                .then().statusCode(HttpStatus.SC_OK).body(containsString("ping -> true"));
    }

}
