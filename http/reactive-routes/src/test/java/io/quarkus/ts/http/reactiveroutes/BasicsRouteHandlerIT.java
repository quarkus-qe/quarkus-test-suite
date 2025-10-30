package io.quarkus.ts.http.reactiveroutes;

import static io.restassured.RestAssured.given;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;

@QuarkusScenario
@DisabledOnSemeruJdk(reason = "https://github.com/eclipse-openj9/openj9/issues/22812")
public class BasicsRouteHandlerIT {

    @Test
    public void shouldWorkUsingParamsWithUnderscore() {
        given().when()
                .get("/basics/param-with-underscore/work")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
