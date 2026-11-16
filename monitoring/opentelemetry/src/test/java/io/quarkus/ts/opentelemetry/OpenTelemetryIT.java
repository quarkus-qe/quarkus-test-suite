package io.quarkus.ts.opentelemetry;

import static io.quarkus.test.bootstrap.Protocol.HTTP;
import static io.quarkus.ts.opentelemetry.MicroProfileTelemetryDIResource.LONG_ATTRIBUTE_NAME;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.wildfly.common.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
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

    private static final Integer SPAN_KIND_CLIENT = 2;
    private static final Integer SPAN_KIND_SERVER = 3;
    // Jaeger v3 API requires a time window (v1 API did not)
    String startMin = Instant.now().minus(1, ChronoUnit.HOURS).toString();
    String startMax = Instant.now().plus(1, ChronoUnit.HOURS).toString();

    @JaegerContainer()
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = { MicroProfileTelemetryDIResource.class,
            PongResource.class }, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms")
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class })
    static final RestService pingservice = new RestService()
            .withProperty("quarkus.application.name", "pingservice")
            .withProperty("pongservice.url", () -> pongservice.getURI(HTTP).getRestAssuredStyleUri())
            .withProperty("pongservice.port", () -> Integer.toString(pongservice.getURI(HTTP).getPort()))
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            // test exporter OTLP proxy is disabled by default
            .withProperty("quarkus.otel.exporter.otlp.traces.proxy-options.host", "Host that must be ignored!")
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms")
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    @Order(1)
    @Test
    public void testContextPropagation() {
        String operationName = "GET /ping/pong";
        String[] operations = new String[] { "GET /ping/pong", "GET /hello", "GET /hello" };
        Arrays.sort(operations);
        whenDoPingPongRequest();
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            Response response = retrieveTraces(pingservice.getName(), operationName);
            String[] operationNames = response.jsonPath().getString("result.resourceSpans*.scopeSpans*.spans*.name")
                    .replace("[", "")
                    .replace("]", "")
                    .split(", ");
            Arrays.sort(operationNames);
            assertArrayEquals(operations, operationNames);
            verifyStandardSourceCodeAttributesArePresent(response, operationName);

            String query = String.format(
                    "result.resourceSpans.scopeSpans.spans.flatten().findAll { it.name == '%s' }.kind.flatten()",
                    "GET /hello");
            List<Integer> spanKinds = response.jsonPath().getList(query);
            Assertions.assertTrue(spanKinds.contains(SPAN_KIND_CLIENT));
            Assertions.assertTrue(spanKinds.contains(SPAN_KIND_SERVER));
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
            Response response = retrieveTraces(pongservice.getName(), operationName);
            verifyAttributeLength(response, operationName, LONG_ATTRIBUTE_NAME);
        });
    }

    private void verifyStandardSourceCodeAttributesArePresent(Response response, String operationName) {
        verifyAttributeValue(response, operationName, "code.function.name", PingResource.class.getName() + ".callPong");
    }

    private void verifyAttributeValue(Response response, String operationName, String attributeName, String attributeValue) {
        response.then().body(getGPathForOperationAndAttribute(operationName, attributeName), is(attributeValue));
    }

    private void verifyAttributeLength(Response response, String operationName, String attributeName) {
        var attrVal = response.body().jsonPath().getString(getGPathForOperationAndAttribute(operationName, attributeName));
        assertNotNull(attrVal);
        assertEquals(51, attrVal.length());
    }

    private static String getGPathForOperationAndAttribute(String operationName, String attribute) {
        return String.format(
                "result.resourceSpans.scopeSpans.spans.flatten().find { it.name == '%s' }.attributes.find { it.key == '%s' }.value.stringValue",
                operationName, attribute);
    }

    private void assureTraceIdPropagatedToJaeger(String path, String spanId) {
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            Response response = retrieveTraces(pongservice.getName(), "GET /mp-telemetry-di/" + path);
            assertTrue(response.body().asString().contains(spanId));
        });
    }

    public void whenDoPingPongRequest() {
        given().when()
                .get(pingservice.getURI(HTTP).withPath("/ping/pong").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalToIgnoringCase("ping pong"));
    }

    private Response retrieveTraces(String serviceName, String operationName) {
        return given().when()
                .queryParam("query.service_name", serviceName)
                .queryParam("query.operation_name", operationName)
                .queryParam("query.start_time_min", startMin)
                .queryParam("query.start_time_max", startMax)
                .get(jaeger.getTraceV3Url())
                .then().statusCode(200).log().all().extract().response();
    }

    private static Map.Entry<String, String> getSpanIdFromPath(String path) {
        return Map.entry(path, given().get(getBaseUri() + path).then().statusCode(200).extract().body().asString());
    }

    private static String getBaseUri() {
        return pongservice.getURI(HTTP).toString() + "/mp-telemetry-di/";
    }
}
