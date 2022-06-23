package io.quarkus.ts.micrometer.prometheus;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class MicroProfileApiIT {

    private static final String PING_PONG = "ping pong";
    private static final String PING_PONG_ENDPOINT = "/using-microprofile-pingpong";
    private static final String COUNTER_FORMAT = "simple_counter_mp_total{scope=\"application\",} %s.0";
    private static final String FIRST_GAUGE_FORMAT = "first_gauge_mp{scope=\"application\",} %s";
    private static final String SECOND_GAUGE_FORMAT = "second_gauge_mp{scope=\"application\",} %s";
    private static final String THIRD_GAUGE_FORMAT = "getThirdGauge{scope=\"application\",} %s";
    private static final long DEFAULT_GAUGE_VALUE = 100;
    private static final long GAUGE_INCREMENT = 1;

    @Test
    public void testShouldReturnCountOne() {
        whenCallCounter();
        thenCounterIs(1);
    }

    @Tag("QUARKUS-1545")
    @Test
    public void testShouldReturnAndModifyGauges() {
        final long incrementedGaugeValue = DEFAULT_GAUGE_VALUE + GAUGE_INCREMENT;
        whenCallGauges(GAUGE_INCREMENT, incrementedGaugeValue * 3);
        thenGaugesAreSampled(incrementedGaugeValue);
        whenCallGauges(-GAUGE_INCREMENT, DEFAULT_GAUGE_VALUE * 3);
        thenGaugesAreSampled(DEFAULT_GAUGE_VALUE);
    }

    private void whenCallCounter() {
        callPingPong(given(), "/counter", PING_PONG);
    }

    private void whenCallGauges(long increment, long expectedGaugeValue) {
        callPingPong(given().queryParam("inc", increment), "/gauges", String.valueOf(expectedGaugeValue));
    }

    private void callPingPong(RequestSpecification requestSpecification, String path, String expectedBody) {
        requestSpecification.when()
                .get(PING_PONG_ENDPOINT + path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is(expectedBody));
    }

    private void thenCounterIs(int expectedCounter) {
        callMetrics().body(containsString(String.format(COUNTER_FORMAT, expectedCounter)));
    }

    private void thenGaugesAreSampled(double expectedGaugeValue) {
        callMetrics().body(
                containsString(String.format(FIRST_GAUGE_FORMAT, expectedGaugeValue)),
                containsString(String.format(SECOND_GAUGE_FORMAT, expectedGaugeValue)),
                containsString(String.format(THIRD_GAUGE_FORMAT, expectedGaugeValue)));
    }

    private ValidatableResponse callMetrics() {
        return when()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
