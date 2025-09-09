package io.quarkus.ts.http.restclient.reactive;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.validation.ValidatedUser;
import io.restassured.http.ContentType;

@QuarkusScenario
@Tag("QUARKUS-6262")
public class UserProcessingValidationIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void shouldFailOnInvalidUser() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new ValidatedUser(null))
                .when().post("/validator-check")
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldAcceptValidUser() {
        app.given()
                .contentType(ContentType.JSON)
                .body(new ValidatedUser("alice"))
                .when().post("/validator-check")
                .then()
                .statusCode(200)
                .body("username", equalTo("alice"));
    }
}
