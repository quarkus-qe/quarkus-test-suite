package io.quarkus.ts.security.jwt;

import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-7342")
@QuarkusScenario
public class BasicHigherThanJwtPriorityIT {
    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("jwt-basic-priority.properties")
            .withProperty("quarkus.smallrye-jwt.priority", "1")
            .withProperty("quarkus.http.auth.basic.priority", "2");

    @Test
    public void shouldChallengeWithBasicWhenPriorityIsHigherThanJwt() {
        app.given()
                .when()
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .header("WWW-Authenticate", containsString("basic"));
    }

}
