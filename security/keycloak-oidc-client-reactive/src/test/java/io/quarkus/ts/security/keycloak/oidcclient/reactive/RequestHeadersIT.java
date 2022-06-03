package io.quarkus.ts.security.keycloak.oidcclient.reactive;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class RequestHeadersIT {

    static final String REALM_DEFAULT = "test-realm";
    static final int KEYCLOAK_PORT = 8080;

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @Container(image = "${keycloak.image}", expectedLog = "Http management interface listening", port = KEYCLOAK_PORT)
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT)
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl());

    @Test
    public void authorizationHeaderDoesNotRepeat() {
        final Response response = given()
                .when()
                .get("/client-request-headers/authorization-repeatedly")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .extract()
                .response();
        final List<Object> authHeaders = response.jsonPath().getList("$");
        Assertions.assertEquals(1, authHeaders.size(), "There must be exactly one Authorization request header.");
    }
}
