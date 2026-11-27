package io.quarkus.ts.javaee.gettingstarted;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class OpenAPITracingIT {
    // Jaeger v3 API requires a time window (v1 API did not)
    String startMin = Instant.now().minus(1, ChronoUnit.HOURS).toString();
    String startMax = Instant.now().plus(1, ChronoUnit.HOURS).toString();

    private static final String SUPPRESS_NON_APPLICATION_URIS = "quarkus.otel.traces.suppress-non-application-uris";
    private static final String URL_PATH_TAG = "url.path";
    private static final String HTTP_RESPONSE_STATUS_CODE_TAG = "http.response.status_code";
    private static final String HTTP_REQUEST_METHOD_TAG = "http.request.method";
    private static final String OPEN_API_PATH = "/q/openapi";
    private static final String OPEN_API_OPERATION = "GET /q/openapi";
    private static final String SWAGGER_UI_PATH = "/q/swagger-ui/";
    private static final String SWAGGER_UI_OPERATION = "GET /q/swagger-ui";

    @JaegerContainer()
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = GreetingResource.class)
    static final RestService tracedApp = new RestService()
            .withProperty("quarkus.application.name", "tracedApp")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms")
            .withProperty(SUPPRESS_NON_APPLICATION_URIS, "false");

    @QuarkusApplication(classes = GreetingResource.class)
    static final RestService untracedApp = new RestService()
            .withProperty("quarkus.application.name", "untracedApp")
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d")
            .withProperty("quarkus.otel.bsp.schedule.delay", "700ms")
            .withProperty(SUPPRESS_NON_APPLICATION_URIS, "true");

    @Test
    public void openApiTraced() {
        testTraced(OPEN_API_PATH, OPEN_API_OPERATION);
    }

    @Test
    public void swaggerUiTraced() {
        testTraced(SWAGGER_UI_PATH, SWAGGER_UI_OPERATION);
    }

    @Test
    public void openApiUntraced() {
        testUntraced(OPEN_API_PATH, OPEN_API_OPERATION);
    }

    @Test
    public void swaggerUiUntraced() {
        testUntraced(SWAGGER_UI_PATH, SWAGGER_UI_OPERATION);
    }

    private void testTraced(String path, String operation) {
        callEndpoint(tracedApp, path);
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Response response = retrieveTraces(tracedApp.getName(), operation);
                    response.then().body("result.resourceSpans.size()", is(1));
                    verifySpansSize(response);
                    verifySpanOperationName(response, operation);
                    verifySpanTag(response, URL_PATH_TAG, path);
                    verifySpanTag(response, HTTP_RESPONSE_STATUS_CODE_TAG, HttpStatus.SC_OK);
                    verifySpanTag(response, HTTP_REQUEST_METHOD_TAG, "GET");
                });
    }

    private void testUntraced(String path, String operation) {
        callEndpoint(untracedApp, path);
        await()
                .during(10, TimeUnit.SECONDS)
                .atMost(12, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Response response = retrieveTraces(untracedApp.getName(), operation);
                    response.then().statusCode(HttpStatus.SC_NOT_FOUND);
                    response.then().body(containsString("No traces found"));
                });
    }

    private void callEndpoint(RestService app, String path) {
        app.given()
                .get(path)
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private Response retrieveTraces(String serviceName, String operationName) {
        return given().when()
                .queryParam("query.service_name", serviceName)
                .queryParam("query.operation_name", operationName)
                .queryParam("query.start_time_min", startMin)
                .queryParam("query.start_time_max", startMax)
                .get(jaeger.getTraceV3Url())
                .then().log().all().extract().response();
    }

    private void verifySpansSize(Response response) {
        response.then().body("result.resourceSpans[0].scopeSpans[0].spans.size()", is(1));
    }

    private void verifySpanOperationName(Response response, String operationName) {
        response.then().body("result.resourceSpans[0].scopeSpans[0].spans[0].name", is(operationName));
    }

    private void verifySpanTag(Response response, String tagKey, String tagValue) {
        response.then().body(String.format(
                "result.resourceSpans[0].scopeSpans[0].spans[0].attributes.find { it.key == '%s' }.value.stringValue",
                tagKey),
                is(tagValue));
    }

    private void verifySpanTag(Response response, String tagKey, int tagValue) {
        response.then().body(String.format(
                "result.resourceSpans[0].scopeSpans[0].spans[0].attributes.find { it.key == '%s' }.value.intValue",
                tagKey),
                is(String.valueOf(tagValue))); // data in spans is stored like this: "intValue": "200"
    }
}
