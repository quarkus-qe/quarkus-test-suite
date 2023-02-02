package io.quarkus.ts.security.keycloak.oidcclient.reactive.basic;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class KeycloakOidcClientSecurityIT extends BaseOidcClientSecurityIT {

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @KeycloakContainer(command = {
            "start-dev --import-realm --hostname-strict-https=false --features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService("/keycloak-realm.json", REALM_DEFAULT, "/realms")
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl());

    @Override
    protected KeycloakService getKeycloak() {
        return keycloak;
    }

    @Override
    protected RestService getApp() {
        return app;
    }

    @Test
    @Disabled("https://github.com/quarkusio/quarkus/issues/30814")
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
