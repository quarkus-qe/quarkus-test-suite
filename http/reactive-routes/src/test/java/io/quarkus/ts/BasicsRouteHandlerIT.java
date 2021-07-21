package io.quarkus.ts;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class BasicsRouteHandlerIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void shouldWorkUsingParamsWithUnderscore() {
        app.given().when()
                .get("/basics/param-with-underscore/work")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
