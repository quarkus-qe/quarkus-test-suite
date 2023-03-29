package io.quarkus.qe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocalEndpointsIT {

    @QuarkusApplication
    static final RestService app = new RestService();

    @Test
    @Order(1)
    public void greeting() {
        Response response = app.given().get("/ping");
        assertEquals(200, response.statusCode());
        assertEquals("pong", response.body().asString());
    }

    @Test
    public void health() {
        app.management().get("q/health").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void oldHealth() {
        app.given().get("q/health").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void metrics() {
        Response response = app.management().get("q/metrics");
        assertEquals(200, response.statusCode());
        String metric = null;
        String body = response.body().asString();
        for (String line : body.split("\n")) {
            if (line.contains("http_server_requests_seconds_count") && line.contains("SUCCESS")) {
                metric = line;
            }
        }
        assertNotNull(metric, "Metric 'http_server_requests_seconds_count' was not found in the response: " + body);
        assertTrue(metric.endsWith("1.0"), "requests count is wrong: " + metric);
    }
}
