package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

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
}
