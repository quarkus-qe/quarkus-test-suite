package io.quarkus.ts.transactions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@DisabledOnNative
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class SwaggerUiIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService();

    @Test
    public void smokeTestSwaggerUi() {
        given()
                .when().get("/q/swagger-ui")
                .then()
                .statusCode(200)
                .body(containsString("/openapi"));
    }
}
