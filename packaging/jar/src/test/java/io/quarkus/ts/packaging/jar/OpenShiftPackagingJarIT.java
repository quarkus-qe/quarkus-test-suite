package io.quarkus.ts.packaging.jar;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
class OpenShiftPackagingJarIT {
    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void packagingTest() throws IOException {
        app.given()
                .get("/hello")
                .then()
                .statusCode(SC_OK)
                .body(is("Hello from jar packaged resource"));
    }
}
