package io.quarkus.ts.micrometer.prometheus;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class UsingMicroProfilePingPongResourceIT {

    private static final String PING_PONG = "ping pong";
    private static final String PING_PONG_ENDPOINT = "/using-microprofile-pingpong";
    private static final String COUNTER_FORMAT = "simple_counter_mp_total{scope=\"application\",} %s.0";
    private static final String GAUGE_FORMAT = "simple_gauge_mp{scope=\"application\",} %s";
    private static final long DEFAULT_GAUGE_VALUE = 100;

    @Test
    public void testShouldReturnCountOne() {
        whenCallPingPong("/counter", PING_PONG);
        thenCounterIs(1);
    }

    @Test
    public void testShouldReturnDefaultGauge() {
        whenCallPingPong("/gauge", "" + DEFAULT_GAUGE_VALUE);
        thenGaugeIs(DEFAULT_GAUGE_VALUE);
    }

    private void whenCallPingPong(String path, String expectedBody) {
        given()
                .when().get(PING_PONG_ENDPOINT + path)
                .then().statusCode(HttpStatus.SC_OK)
                .body(is(expectedBody));
    }

    private void thenCounterIs(int expectedCounter) {
        when().get("/q/metrics").then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(String.format(COUNTER_FORMAT, expectedCounter)));
    }

    private void thenGaugeIs(double expectedGauge) {
        when().get("/q/metrics").then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(String.format(GAUGE_FORMAT, expectedGauge)));
    }
}
