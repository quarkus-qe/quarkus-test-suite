package io.quarkus.qe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@Disabled("Requires fixing https://github.com/quarkusio/quarkus/issues/32135 and changes in the Framework")
public class OpenShiftExtensionIT {
    @QuarkusApplication
    static final RestService app = new RestService();

    @Test
    public void payload() {
        Response response = app.given().get("/ping");
        assertEquals(200, response.statusCode());
        assertEquals("pong", response.body().asString());
    }

    @Test
    public void health() {
        app.management().get("q/health").then().statusCode(HttpStatus.SC_OK);
    }
}
