package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

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
}
