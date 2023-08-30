package io.quarkus.ts.opentelemetry.reactive;

import static io.quarkus.test.bootstrap.Protocol.HTTP;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class OpentelemetryReactiveIT {

    private static final String OTEL_PING_SERVICE_NAME = "pingservice";

    private Response resp;

    @JaegerContainer(useOtlpCollector = true, expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = { PongResource.class, SchedulerResource.class,
            SchedulerService.class }, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class })
    static final RestService pingservice = new RestService()
            .withProperty("pongservice_url", () -> pongservice.getURI(HTTP).getRestAssuredStyleUri())
            .withProperty("pongservice_port", () -> Integer.toString(pongservice.getURI(HTTP).getPort()))
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            // verify OTEL service name has priority over default Quarkus application name
            .withProperty("quarkus.otel.service.name", OTEL_PING_SERVICE_NAME)
            // FIXME: change Quarkus app name when https://github.com/quarkusio/quarkus/issues/33317 is fixed
            .withProperty("quarkus.application.name", OTEL_PING_SERVICE_NAME);

    @Test
    public void testContextPropagation() {
        int pageLimit = 10;
        String operationName = "GET /ping/pong";
        String[] operations = new String[] { "GET /ping/pong", "GET", "GET /hello" };

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            whenDoPingPongRequest();
            thenRetrieveTraces(pageLimit, "1h", OTEL_PING_SERVICE_NAME, operationName);
            thenTriggeredOperationsMustBe(containsInAnyOrder(operations));
            thenTraceSpanSizeMustBe(is(3)); // 2 endpoint's + rest client call
            verifyStandardSourceCodeAttributesArePresent(operationName);
        });
    }

    @Test
    public void testSchedulerTracing() {
        String operationName = "SchedulerService.increment";
        String[] operations = new String[] { operationName };

        // asserts scheduled method was traced
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(10, "1h", pongservice.getName(), operationName);
            thenTriggeredOperationsMustBe(containsInAnyOrder(operations));
            thenTraceSpanSizeMustBe(greaterThanOrEqualTo(1));
        });

        // asserts scheduled method was invoked
        int invocations = Integer.parseInt(given().when()
                .get(pongservice.getURI(HTTP).withPath("/scheduler/count").toString())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString());
        Assertions.assertTrue(invocations >= 2);
    }

    public void whenDoPingPongRequest() {
        given().when()
                .get(pingservice.getURI(HTTP).withPath("/ping/pong").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalToIgnoringCase("ping pong"));
    }

    private void thenRetrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName) {
        resp = given().when()
                .queryParam("operation", operationName)
                .queryParam("lookback", lookBack)
                .queryParam("limit", pageLimit)
                .queryParam("service", serviceName)
                .get(jaeger.getTraceUrl());
    }

    private void thenTraceSpanSizeMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans.size()", matcher);
    }

    private void thenTriggeredOperationsMustBe(Matcher<?> matcher) {
        resp.then().body("data[0].spans.operationName", matcher);
    }

    private void verifyStandardSourceCodeAttributesArePresent(String operationName) {
        verifyAttributeValue(operationName, "code.namespace", PingResource.class.getName());
        verifyAttributeValue(operationName, "code.function", "callPong");
    }

    private void verifyAttributeValue(String operationName, String attributeName, String attributeValue) {
        resp.then().body(getGPathForOperationAndAttribute(operationName, attributeName), is(attributeValue));
    }

    private static String getGPathForOperationAndAttribute(String operationName, String attribute) {
        return String.format("data[0].spans.find { it.operationName == '%s' }.tags.find { it.key == '%s' }.value",
                operationName, attribute);
    }
}
