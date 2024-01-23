package io.quarkus.ts.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusVersion;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class OpenTelemetryManagementIT {
    @JaegerContainer
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService pong = new RestService()
            .withProperty("quarkus.application.name", "pong")
            .withProperty("quarkus.management.enabled", "true")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    private static final String PONG_ENDPOINT = "/hello";
    private static final String MANAGEMENT_ENDPOINT = "/q/health/ready";

    /**
     * Test openTelemetry not sending traces from management endpoints
     */
    @DisabledOnQuarkusVersion(version = "3.2.9.Final", reason = "Fixed in 3.2.10")
    @Test
    @Tag("https://github.com/quarkusio/quarkus/pull/37218")
    public void managementEndpointExcludedFromTracesTest() {
        // invoke management endpoint, so service is prone to send trace from it
        // if fix is already in place, it should not send any
        pong.management().get(MANAGEMENT_ENDPOINT)
                .then().statusCode(HttpStatus.SC_OK);

        // invoke normal endpoint, so we can check that traces are uploading correctly
        given()
                .when().get(PONG_ENDPOINT)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("pong"));

        // wait for pong endpoint to be logged in traces
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> given()
                .when()
                .queryParam("service", pong.getName())
                .get(jaeger.getTraceUrl())
                .then().statusCode(HttpStatus.SC_OK)
                .and().body(containsString(PONG_ENDPOINT)));

        String traces = given().when()
                .queryParam("service", pong.getName())
                .get(jaeger.getTraceUrl())
                .thenReturn().body().asString();

        // check that management endpoint is not present in traces, while correct trace is there
        Assertions.assertTrue(traces.contains(PONG_ENDPOINT), "Pong endpoint should be logged in traces");
        Assertions.assertFalse(traces.contains(MANAGEMENT_ENDPOINT), "Management endpoint should not be logged in traces");
    }
}
