package io.quarkus.ts.opentelemetry.reactive;

import static io.quarkus.test.bootstrap.Protocol.HTTP;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
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

    static final String ADMIN_USERNAME = "alice";
    static final String ADMIN_PASSWORD = "alice";

    @JaegerContainer(useOtlpCollector = true, expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = { PongResource.class, SchedulerResource.class,
            SchedulerService.class }, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class, AdminResource.class })
    static final RestService pingservice = new RestService()
            .withProperty("pongservice.url", () -> pongservice.getURI(HTTP).getRestAssuredStyleUri())
            .withProperty("pongservice.port", () -> Integer.toString(pongservice.getURI(HTTP).getPort()))
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            // verify OTEL service name has priority over default Quarkus application name
            .withProperty("quarkus.otel.service.name", OTEL_PING_SERVICE_NAME);

    @Test
    public void testContextPropagation() {
        int pageLimit = 10;
        String operationName = "GET /ping/pong";
        String[] operations = new String[] { "GET /ping/pong", "GET /hello", "GET /hello" };

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            whenDoPingPongRequest();
            thenRetrieveTraces(pageLimit, "1h", OTEL_PING_SERVICE_NAME, operationName);
            thenTriggeredOperationsMustBe(containsInAnyOrder(operations));
            thenTraceSpanSizeMustBe(is(3)); // 2 endpoint's + rest client call
            verifyStandardSourceCodeAttributesArePresent(operationName);
            ArrayList<String> spanKinds = resp.body().path(
                    "data[0].spans.findAll { it.operationName == '%s' }.tags.flatten().findAll { it.key == 'span.kind' }.value.flatten()",
                    "GET /hello");
            assertTrue(spanKinds.contains("client"));
            assertTrue(spanKinds.contains("server"));
        });
    }

    @Test
    public void testSecurityEvents() {
        int pageLimit = 10;
        String serviceName = "pingservice";
        String operationName = "GET /admin";
        doSecurityEndpointRequest();
        await().atMost(30, TimeUnit.SECONDS).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(pageLimit, "1h", serviceName, operationName);
            assertSecurityEventsAndLogsPresent();
        });

    }

    @Test
    public void testSchedulerTracing() {
        // FIXME: report breaking change if not fixed: https://github.com/quarkusio/quarkus/pull/35989#issuecomment-1836023316
        String operationName = "1_io.quarkus.ts.opentelemetry.reactive.SchedulerService#increment";
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
        assertTrue(invocations >= 2);
    }

    public void whenDoPingPongRequest() {
        given().when()
                .get(pingservice.getURI(HTTP).withPath("/ping/pong").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalToIgnoringCase("ping pong"));
    }

    private void doSecurityEndpointRequest() {
        given()
                .auth().basic(ADMIN_USERNAME, ADMIN_PASSWORD)
                .get(pingservice.getURI(HTTP).withPath("/admin").toString())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, admin " + ADMIN_USERNAME));
    }

    private void thenRetrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName) {
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    resp = given().when()
                            .queryParam("operation", operationName)
                            .queryParam("lookback", lookBack)
                            .queryParam("limit", pageLimit)
                            .queryParam("service", serviceName)
                            .get(jaeger.getTraceUrl());
                    return !resp.jsonPath().getList("data.spans").isEmpty();
                });
    }

    private void assertSecurityEventsAndLogsPresent() {
        resp.then().body(
                "data.flatten().spans.flatten().findAll { span -> span.operationName == 'GET /admin' }.logs.flatten().findAll { log -> log.fields.find { field -> field.key == 'event' && field.value == 'quarkus.security.authorization.success' } }",
                is(not(empty())));
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
