package io.quarkus.ts.javaee.gettingstarted;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

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

    private static final String SUPPRESS_NON_APPLICATION_URIS = "quarkus.otel.traces.suppress-non-application-uris";
    private static final String HTTP_TARGET_TAG = "http.target";
    private static final String HTTP_STATUS_CODE_TAG = "http.status_code";
    private static final String HTTP_METHOD_TAG = "http.method";
    private static final String SPAN_KIND_TAG = "span.kind";
    private static final String OPEN_API_PATH = "/q/openapi";
    private static final String OPEN_API_OPERATION = "GET /openapi";
    private static final String SWAGGER_UI_PATH = "/q/swagger-ui/";
    private static final String SWAGGER_UI_OPERATION = "GET /swagger-ui";

    @JaegerContainer(expectedLog = "\"Health Check state change\",\"status\":\"ready\"")
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication(classes = GreetingResource.class)
    static final RestService tracedApp = new RestService()
            .withProperty("quarkus.application.name", "tracedApp")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
            .withProperty(SUPPRESS_NON_APPLICATION_URIS, "false");

    @QuarkusApplication(classes = GreetingResource.class)
    static final RestService untracedApp = new RestService()
            .withProperty("quarkus.application.name", "untracedApp")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl)
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
                    verifyDataSize(response, 1);
                    verifySpansSize(response);
                    verifySpanOperationName(response, operation);
                    verifySpanTag(response, HTTP_TARGET_TAG, path);
                    verifySpanTag(response, HTTP_STATUS_CODE_TAG, HttpStatus.SC_OK);
                    verifySpanTag(response, HTTP_METHOD_TAG, "GET");
                    verifySpanTag(response, SPAN_KIND_TAG, "server");
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
                    verifyDataSize(response, 0);
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
                .queryParam("service", serviceName)
                .queryParam("operation", operationName)
                .get(jaeger.getTraceUrl());
    }

    private void verifyDataSize(Response response, int size) {
        response.then().body("data.size()", is(size));
    }

    private void verifySpansSize(Response response) {
        response.then().body("data[0].spans.size()", is(1));
    }

    private void verifySpanOperationName(Response response, String operationName) {
        response.then().body("data[0].spans[0].operationName", is(operationName));
    }

    private <T> void verifySpanTag(Response response, String tagKey, T tagValue) {
        response.then().body(String.format("data[0].spans[0].tags.find { it.key == '%s' }.value", tagKey), is(tagValue));
    }
}
