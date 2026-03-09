package io.quarkus.ts.packaging.quarkus;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
class OpenShiftPackagingQuarkusIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void packagingTest() throws IOException {
        app.given()
                .get("/hello")
                .then()
                .statusCode(SC_OK)
                .body(is("Hello from Quarkus packaged resource"));
    }
}
