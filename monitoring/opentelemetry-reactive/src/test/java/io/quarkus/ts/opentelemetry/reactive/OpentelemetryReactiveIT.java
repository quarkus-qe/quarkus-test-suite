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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class OpentelemetryReactiveIT {
    private static final Integer SPAN_KIND_CLIENT = 2;
    private static final Integer SPAN_KIND_SERVER = 3;

    private static final String OTEL_PING_SERVICE_NAME = "pingservice";
    private static final String TRACING_SUPPRESSED_SERVICE_NAME = "tracing-suppressed-service";

    private Response resp;

    static final String ADMIN_USERNAME = "alice";
    static final String ADMIN_PASSWORD = "alice";

    @JaegerContainer()
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = { PongResource.class, SchedulerResource.class,
            SchedulerService.class }, properties = "pong.properties")
    static final RestService pongservice = new RestService()
            .withProperty("quarkus.application.name", "pongservice")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    @QuarkusApplication(classes = { PingResource.class, PingPongService.class, AdminResource.class })
    static final RestService pingservice = new RestService()
            .withProperty("pongservice.url", () -> pongservice.getURI(HTTP).getRestAssuredStyleUri())
            .withProperty("pongservice.port", () -> Integer.toString(pongservice.getURI(HTTP).getPort()))
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            // verify OTEL service name has priority over default Quarkus application name
            .withProperty("quarkus.otel.service.name", OTEL_PING_SERVICE_NAME);

    @QuarkusApplication(classes = { PartiallyTraceableResource.class, UntraceableResource.class })
    static final RestService tracingsuppressedservice = new RestService()
            .withProperty("quarkus.application.name", TRACING_SUPPRESSED_SERVICE_NAME)
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            .withProperty("quarkus.otel.service.name", TRACING_SUPPRESSED_SERVICE_NAME)
            // we need to censor root as OpenShift readiness probe throws in a bunch of GET / traces into the mix and we don't want that in the test
            .withProperty("quarkus.otel.traces.suppress-application-uris", "/,partially-traceable-hello,untraceable-hello*");

    @Test
    public void testContextPropagation() {
        int pageLimit = 10;
        String operationName = "GET /ping/pong";
        String[] operations = new String[] { "GET /ping/pong", "GET /hello", "GET /hello" };

        whenDoPingPongRequest();
        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(pageLimit, "1h", OTEL_PING_SERVICE_NAME, operationName);
            thenTriggeredOperationsMustContain(operations);
            thenTraceSpanSizeMustBe(is(3)); // 2 endpoint's + rest client call
            verifyStandardSourceCodeAttributesArePresent(operationName);
            ArrayList<Integer> spanKinds = resp.body().path(
                    "result.resourceSpans.scopeSpans.spans.flatten().findAll { it.name == '%s' }.kind.flatten()",
                    "GET /hello");
            assertTrue(spanKinds.contains(SPAN_KIND_CLIENT));
            assertTrue(spanKinds.contains(SPAN_KIND_SERVER));
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
            thenTriggeredOperationsMustContain(operations);
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

    @Test
    @Tag("QUARKUS-5668")
    public void testUriTracingSuppression() {
        String queryParamOperationName = "GET /partially-traceable-hello";
        String pathParamOperationName = "GET /partially-traceable-hello/{name}";
        String subPathOperationName = "GET /partially-traceable-hello/everybody";
        String[] operations = new String[] { queryParamOperationName, pathParamOperationName, subPathOperationName };
        doSuppressedTracingRequests();

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            thenRetrieveTraces(10, "1h", TRACING_SUPPRESSED_SERVICE_NAME);
            resp.then().body("result.resourceSpans.scopeSpans.spans.flatten().name", containsInAnyOrder(operations));
            thenNumberOfTracesMustBe(is(3)); // untraceable-hello is fully suppressed, /partially-traceable-hello root is suppressed, three other requests should be traced
        });
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

    private void doSuppressedTracingRequests() {
        given().when()
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/untraceable-hello").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Untraced hello anonymous"));

        given().when()
                .queryParam("name", "alice")
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/untraceable-hello").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Untraced hello alice"));

        given().when()
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/untraceable-hello/bob").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Untraced hello bob"));

        given().when()
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/untraceable-hello/everybody").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Untraced hello to everybody!"));

        given().when()
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/partially-traceable-hello").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Untraced hello anonymous"));

        given().when()
                .queryParam("name", "alice")
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/partially-traceable-hello").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Traced hello alice"));

        given().when()
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/partially-traceable-hello/bob").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Traced hello bob"));

        given().when()
                .get(tracingsuppressedservice.getURI(HTTP).withPath("/partially-traceable-hello/everybody").toString())
                .then()
                .statusCode(HttpStatus.SC_OK).body(equalTo("Traced hello to everybody!"));

    }

    private void thenRetrieveTraces(int pageLimit, String lookBack, String serviceName, String operationName) {
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    resp = given().when()
                            .queryParam("query.operation_name", operationName)
                            .queryParam("lookback", lookBack)
                            .queryParam("limit", pageLimit)
                            .queryParam("query.service_name", serviceName)
                            .queryParam("query.start_time_min", Instant.now().minus(1, ChronoUnit.HOURS).toString())
                            .queryParam("query.start_time_max", Instant.now().plus(1, ChronoUnit.HOURS).toString())
                            .get(jaeger.getTraceV3Url());
                    List<Object> traces = resp.jsonPath().getList("result.resourceSpans.scopeSpans.spans.flatten()");
                    return traces != null && !traces.isEmpty();
                });
    }

    private void thenRetrieveTraces(int pageLimit, String lookBack, String serviceName) {
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    resp = given().when()
                            .queryParam("lookback", lookBack)
                            .queryParam("limit", pageLimit)
                            .queryParam("query.service_name", serviceName)
                            .queryParam("query.start_time_min", Instant.now().minus(1, ChronoUnit.HOURS).toString())
                            .queryParam("query.start_time_max", Instant.now().plus(1, ChronoUnit.HOURS).toString())
                            .get(jaeger.getTraceV3Url());
                    List<Object> traces = resp.jsonPath().getList("result.resourceSpans.scopeSpans.spans.flatten()");
                    return traces != null && !traces.isEmpty();
                });
    }

    private void assertSecurityEventsAndLogsPresent() {
        resp.then().body(
                "result.resourceSpans.scopeSpans.spans.flatten().findAll { span -> span.name == 'GET /admin' }.events.flatten().findAll { event -> event.name == 'quarkus.security.authorization.success' }",
                is(not(empty())));
    }

    private void thenNumberOfTracesMustBe(Matcher<?> matcher) {
        resp.then().body("result.resourceSpans.size()", matcher);
    }

    private void thenTraceSpanSizeMustBe(Matcher<?> matcher) {
        resp.then().body("result.resourceSpans.scopeSpans.spans.flatten().size()", matcher);
    }

    private void thenTriggeredOperationsMustContain(String[] operations) {
        List<String> spans = resp.jsonPath().getList("result.resourceSpans.scopeSpans.spans.name.flatten()");
        List<String> cleanedSpans = spans.stream()
                .map(line -> line.replace("[", ""))
                .map(line -> line.replace("]", ""))
                .toList();
        for (String operation : operations) {
            Assertions.assertTrue(cleanedSpans.contains(operation), cleanedSpans + " should contain " + operation);
        }
    }

    private void verifyStandardSourceCodeAttributesArePresent(String operationName) {
        verifyAttributeValue(operationName, "code.function.name", PingResource.class.getName() + ".callPong");
    }

    private void verifyAttributeValue(String operationName, String attributeName, String attributeValue) {
        resp.then().body(getGPathForOperationAndAttribute(operationName, attributeName), is(attributeValue));
    }

    private static String getGPathForOperationAndAttribute(String operationName, String attribute) {
        return String.format(
                "result.resourceSpans.scopeSpans.spans.flatten().find { it.name == '%s' }.attributes.find { it.key == '%s' }.value.stringValue",
                operationName, attribute);
    }
}
