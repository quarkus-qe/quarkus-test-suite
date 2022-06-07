package io.quarkus.ts.http.undertow;

import static io.quarkus.ts.http.undertow.listener.SessionListener.GAUGE_ACTIVE_SESSION;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.apache.http.util.Asserts;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;

@QuarkusScenario
public class HttpServletWithSessionListenerIT {

    static final Duration ACTIVE_SESSION_TIMEOUT = Duration.ofMinutes(2);
    static final Duration REST_ASSURANCE_POLL_INTERVAL = Duration.ofSeconds(1);

    @Test
    public void sessionEviction() {
        int activeSessions = 20;
        thenMakeHelloWorldQuery(activeSessions);
        thenCheckActiveSessionsEqualTo(activeSessions);
        thenWaitToEvictSessionsAndCheckActiveSessionsEqualTo(0);
    }

    private double getActiveSessions() {
        return (Double) RestAssured.given().when()
                .get("/app/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK).extract().as(Map.class).get(GAUGE_ACTIVE_SESSION);
    }

    private void thenMakeHelloWorldQuery(int requestAmount) {
        IntStream.range(0, requestAmount).forEach(i -> RestAssured.given().when()
                .get("/app/servlet/hello")
                .then()
                .statusCode(HttpStatus.SC_OK));
    }

    private void thenCheckActiveSessionsEqualTo(int threshold) {
        Asserts.check(getActiveSessions() == threshold, "Unexpected active sessions amount");
    }

    private void thenWaitToEvictSessionsAndCheckActiveSessionsEqualTo(int value) {
        await()
                .atLeast(Duration.ofSeconds(50))
                .atMost(ACTIVE_SESSION_TIMEOUT)
                .with()
                .pollInterval(REST_ASSURANCE_POLL_INTERVAL)
                .until(() -> getActiveSessions() == value);
    }
}
