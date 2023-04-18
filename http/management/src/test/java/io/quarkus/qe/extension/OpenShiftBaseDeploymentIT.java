package io.quarkus.qe.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

public abstract class OpenShiftBaseDeploymentIT {
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
