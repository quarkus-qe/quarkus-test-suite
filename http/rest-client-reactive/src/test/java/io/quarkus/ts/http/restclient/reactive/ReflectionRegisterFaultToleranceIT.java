package io.quarkus.ts.http.restclient.reactive;

import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class ReflectionRegisterFaultToleranceIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    void testRegisterAnnotationWorksWithReflection() {
        app.given().get("/ft-reflection")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("OK"));
    }
}
