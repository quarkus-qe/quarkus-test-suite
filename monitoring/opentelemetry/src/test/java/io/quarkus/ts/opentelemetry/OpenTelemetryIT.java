package io.quarkus.ts.opentelemetry;

import static io.quarkus.test.bootstrap.Protocol.HTTP;
import static io.quarkus.ts.opentelemetry.MicroProfileTelemetryDIResource.LONG_ATTRIBUTE_NAME;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // order methods as SDK autoconfigure test expects previous traces
@QuarkusScenario
public class OpenTelemetryIT {

    private Response resp;

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = { MicroProfileTelemetryDIResource.class,
            PongResource.class }, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class })
    static final RestService pingservice = new RestService()
            .withProperty("quarkus.application.name", "pingservice")
            .withProperty("pongservice_url", () -> pongservice.getURI(HTTP).getRestAssuredStyleUri())
            .withProperty("pongservice_port", () -> Integer.toString(pongservice.getURI(HTTP).getPort()))
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    @Order(1)
    @Test
    public void testContextPropagation() {
        int pageLimit = 10;
        String operationName = "GET /ping/pong";
        String[] operations = new String[] { "GET /ping/pong", "GET", "GET /hello" };

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            whenDoPingPongRequest();
            thenRetrieveTraces(pageLimit, "1h", pingservice.getName(), operationName);
            thenTriggeredOperationsMustBe(containsInAnyOrder(operations));
            thenTraceSpanSizeMustBe(is(3)); // 2 endpoint's + rest client call
            verifyStandardSourceCodeAttributesArePresent(operationName);
        });
    }

    @Order(2)
    @Test
    void testInjectionOfMicroProfileTelemetryBeans() {
        // first create traces
        var pathToSpanId = Stream.of(
                getSpanIdFromPath("span"), // get Span id from injected Span
                getSpanIdFromPath("tracer"), // build Span id from injected Tracer and return Span id
                getSpanIdFromPath("otel") // build Span id from injected OpenTelemetry and return Span id
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // give some time to propagate traces to by checking Baggage now
        // assure Baggage is injected
        given().get(getBaseUri() + "baggage").then().statusCode(200).body(is("true"));

        // now assure all spans were propagated to Jaeger
        pathToSpanId.forEach(this::assureTraceIdPropagatedToJaeger);
    }

    @Order(3)
    @Test
    void testSdkAutoconfiguration() {
        // test setting quarkus.otel.attribute.value.length.limit (set to 51) has effect and
        // attribute of length 54 is cut down to 51

        String operationName = "GET /mp-telemetry-di/span";
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(10, "1h", pongservice.getName(), operationName);
            verifyAttributeLength(operationName, LONG_ATTRIBUTE_NAME);
        });
    }

    private void verifyStandardSourceCodeAttributesArePresent(String operationName) {
        verifyAttributeValue(operationName, "code.namespace", PingResource.class.getName());
        verifyAttributeValue(operationName, "code.function", "callPong");
    }

    private void verifyAttributeValue(String operationName, String attributeName, String attributeValue) {
        resp.then().body(getGPathForOperationAndAttribute(operationName, attributeName), is(attributeValue));
    }

    private void verifyAttributeLength(String operationName, String attributeName) {
        var attrVal = resp.body().jsonPath().getString(getGPathForOperationAndAttribute(operationName, attributeName));
        assertNotNull(attrVal);
        assertEquals(51, attrVal.length());
    }

    private static String getGPathForOperationAndAttribute(String operationName, String attribute) {
        return String.format("data[0].spans.find { it.operationName == '%s' }.tags.find { it.key == '%s' }.value",
                operationName, attribute);
    }

    private void assureTraceIdPropagatedToJaeger(String path, String spanId) {
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(200, "1h", pongservice.getName(), "GET /mp-telemetry-di/" + path);
            assertTrue(resp.body().asString().contains(spanId));
        });
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

    private static Map.Entry<String, String> getSpanIdFromPath(String path) {
        return Map.entry(path, given().get(getBaseUri() + path).then().statusCode(200).extract().body().asString());
    }

    private static String getBaseUri() {
        return pongservice.getURI(HTTP).toString() + "/mp-telemetry-di/";
    }
}
