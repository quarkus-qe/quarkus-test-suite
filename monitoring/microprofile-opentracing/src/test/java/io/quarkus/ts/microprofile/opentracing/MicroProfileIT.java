package io.quarkus.ts.microprofile.opentracing;

import static io.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;

/**
 * Must be ordered to verify the traces in Jaeger.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
public class MicroProfileIT {

    private static final int EXPECTED_SPANS_SIZE = 3;
    private static final int EXPECTED_DATA_SIZE = 1;

    private static final String SERVICE_NAME = "test-traced-service";
    private static final int TIMEOUT_SEC = 59;
    private static final int POLL_DELAY_SEC = 10;

    @JaegerContainer
    static JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.jaeger.service-name", SERVICE_NAME)
            .withProperty("quarkus.jaeger.endpoint", jaeger::getCollectorUrl);

    @Order(1)
    @Test
    public void helloTest() {
        with().pollInterval(Duration.ofSeconds(1)).and()
                .with().pollDelay(Duration.ofSeconds(POLL_DELAY_SEC)).await()
                .atLeast(Duration.ofSeconds(1))
                .atMost(TIMEOUT_SEC, TimeUnit.SECONDS)
                .with()
                .untilAsserted(() -> {
                    app.given().log().uri().when()
                            .get("/client")
                            .then()
                            .statusCode(HttpURLConnection.HTTP_OK)
                            .body(is("Client got: Hello, World!"));
                });
    }

    @Order(2)
    @Test
    public void verifyTracesInJaegerTest() {
        // the tracer inside the application doesn't send traces to the Jaeger server immediately,
        // they are batched, so we need to wait a bit
        await().atMost(TIMEOUT_SEC, TimeUnit.SECONDS).untilAsserted(() -> {
            when()
                    .get(jaeger.getTraceUrl() + "?service=" + SERVICE_NAME)
                    .then()
                    .statusCode(HttpURLConnection.HTTP_OK)
                    .body("data", hasSize(EXPECTED_DATA_SIZE))
                    .body("data[0].spans", hasSize(EXPECTED_SPANS_SIZE))
                    .body("data[0].spans.operationName", hasItems(
                            "GET:io.quarkus.ts.microprofile.opentracing.ClientResource.get",
                            "GET",
                            "GET:io.quarkus.ts.microprofile.opentracing.HelloResource.get"))
                    .body("data[0].spans.logs.fields.value.flatten()", hasItems(
                            "ClientResource called",
                            "HelloResource called",
                            "HelloService called",
                            "HelloService async processing"))
                    .body("data[0].spans.find { "
                            +
                            "it.operationName == 'GET:io.quarkus.ts.microprofile.opentracing.ClientResource.get' }.tags.collect"
                            +
                            " { \"${it.key}=${it.value}\".toString() }",
                            hasItems(
                                    "span.kind=server",
                                    "component=jaxrs",
                                    "http.method=GET",
                                    "http.status_code=200"))
                    .body("data[0].spans.find {"
                            +
                            " it.operationName == 'GET' }.tags.collect { \"${it.key}=${it.value}\".toString() }",
                            hasItems(
                                    "span.kind=client",
                                    "component=jaxrs",
                                    "http.url=http://localhost:" + getAppPort() + "/hello",
                                    "http.method=GET",
                                    "http.status_code=200"))
                    .body("data[0].spans.find { "
                            +
                            "it.operationName == 'GET:io.quarkus.ts.microprofile.opentracing.HelloResource.get' }.tags.collect "
                            +
                            "{ \"${it.key}=${it.value}\".toString() }",
                            hasItems(
                                    "span.kind=server",
                                    "component=jaxrs",
                                    "http.url=http://localhost:" + getAppPort() + "/hello",
                                    "http.method=GET",
                                    "http.status_code=200"));
        });
    }

    @Order(3)
    @Tag("QUARKUS-697")
    @Test
    public void fallbackTest() {
        with().pollInterval(Duration.ofSeconds(1)).and()
                .with().pollDelay(Duration.ofSeconds(POLL_DELAY_SEC)).await()
                .atLeast(Duration.ofSeconds(1))
                .atMost(TIMEOUT_SEC, TimeUnit.SECONDS)
                .with()
                .untilAsserted(() -> {
                    app.given().log().uri().when()
                            .get("/client/fallback")
                            .then()
                            .statusCode(HttpURLConnection.HTTP_OK)
                            .body(is("Client got: Fallback"));
                });
    }

    protected int getAppPort() {
        return app.getPort();
    }
}
