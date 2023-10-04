package io.quarkus.ts.http.undertow;

import static io.quarkus.ts.http.undertow.listener.SessionListener.GAUGE_ACTIVE_SESSION;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.apache.http.util.Asserts;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
public class HttpServletWithSessionListenerIT {

    static final Duration ACTIVE_SESSION_TIMEOUT = Duration.ofMinutes(2);
    static final Duration REST_ASSURANCE_POLL_INTERVAL = Duration.ofSeconds(1);

    @Order(1)
    @Test
    public void sessionEviction() {
        int activeSessions = 20;
        thenMakeHelloWorldQuery(activeSessions);
        thenCheckActiveSessionsEqualTo(activeSessions);
        thenWaitToEvictSessionsAndCheckActiveSessionsEqualTo(0);
    }

    @Tag("QUARKUS-2819")
    @Order(2)
    @Test
    public void sessionSecured() {
        // main objection is to test that CDI request scope can be activated during auth without
        // having issue to use request scope later during processing
        // session secured portion of this test is in order to stick to a test class theme
        thenMakeSecuredWorldQuery("Rambo", 401);
        thenCheckActiveSessionsEqualTo(0);
        thenMakeSecuredWorldQuery("Rocky", 403);
        thenCheckActiveSessionsEqualTo(0);
        thenMakeSecuredWorldQuery("Pablo", 200).body(Matchers.is("secured-servlet-value"));
        thenCheckActiveSessionsEqualTo(1);
    }

    private double getActiveSessions() {
        var activeSessions = (Double) RestAssured.given().when()
                .get("/app/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK).extract().as(Map.class).get(GAUGE_ACTIVE_SESSION);
        if (activeSessions == null) {
            return 0;
        }
        return activeSessions;
    }

    private ValidatableResponse thenMakeSecuredWorldQuery(String user, int httpStatus) {
        return RestAssured.given().when()
                .auth().basic(user, user)
                .queryParam("secured-servlet-key", "secured-servlet-value")
                .get("/app/servlet/secured")
                .then()
                .statusCode(httpStatus);
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
