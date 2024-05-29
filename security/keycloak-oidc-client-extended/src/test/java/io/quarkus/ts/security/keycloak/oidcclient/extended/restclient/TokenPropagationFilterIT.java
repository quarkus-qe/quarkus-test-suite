package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.Response;

@Tag("QUARKUS-3680")
@QuarkusScenario
public class TokenPropagationFilterIT extends BaseOidcIT {

    @Test
    public void usernameTest() {
        given()
                .auth().oauth2(createToken())
                .when().get("/token-propagation-filter")
                .then().statusCode(HttpStatus.SC_OK)
                .body(containsString(USER));
    }

    @Test
    public void jsonUsernameTest() {
        Response response = given()
                .auth().oauth2(createToken())
                .when().get("/json-propagation-filter");
        Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());
        Assertions.assertEquals(USER, response.body().asString());
    }
}
