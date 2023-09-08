package io.quarkus.ts.opentelemetry.reactive;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class OpenTelemetryGrpcIT {

    @JaegerContainer(useOtlpCollector = true)
    static final JaegerService jaeger = new JaegerService();

    @QuarkusApplication()
    static RestService app = new RestService()
            .withProperty("quarkus.application.name", "pingpong")
            .withProperty("quarkus.otel.exporter.otlp.traces.endpoint", jaeger::getCollectorUrl);

    private static final String PING_ENDPOINT = "/grpc-ping";
    private static final String PONG_ENDPOINT = "/grpc-pong";
    private static final String SAY_PONG_PROTO = "SayPong";

    @Test
    public void testServerClientTrace() {
        // When calling ping, the rest will invoke also the pong rest endpoint.
        given()
                .when().get(PING_ENDPOINT)
                .then().statusCode(HttpStatus.SC_OK)
                .body(containsString("ping pong"));

        // Then both ping and pong rest endpoints should have the same trace Id.
        String pingTraceId = given()
                .when().get(PING_ENDPOINT + "/lastTraceId")
                .then().statusCode(HttpStatus.SC_OK).and().extract().asString();

        assertTraceIdWithPongService(pingTraceId);

        // Then Jaeger is invoked
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> given()
                .when().get(jaeger.getTraceUrl() + "?traceID=" + pingTraceId)
                .then().statusCode(HttpStatus.SC_OK)
                .and().body(allOf(containsString(PING_ENDPOINT), containsString(SAY_PONG_PROTO))));
    }

    @Test
    void metrics() {
        Response response = given().get("q/metrics");
        assertEquals(200, response.statusCode());
        String body = response.body().asString();
        Map<String, String> metrics = Arrays.stream(body.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .filter(line -> line.contains("grpc"))
                .map(line -> line.split(" "))
                .collect(Collectors.toMap(line -> line[0], line -> line[1]));
        String wrongValueMessage = "Unexpected value of a metric in the list: " + metrics;
        assertEquals("1.0", metrics.get(
                "grpc_server_requests_received_messages_total{method=\"SayPong\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_server_requests_received_messages_total{method=\"ReturnLastTraceId\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_server_responses_sent_messages_total{method=\"SayPong\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_server_responses_sent_messages_total{method=\"ReturnLastTraceId\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_client_responses_received_messages_total{method=\"SayPong\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_client_responses_received_messages_total{method=\"ReturnLastTraceId\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_client_requests_sent_messages_total{method=\"SayPong\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        assertEquals("1.0", metrics.get(
                "grpc_client_requests_sent_messages_total{method=\"ReturnLastTraceId\",methodType=\"UNARY\",service=\"io.quarkus.example.PongService\"}"),
                wrongValueMessage);
        Set<String> metricNames = metrics.keySet().stream()
                .map(name -> name.split("\\{")[0])
                .collect(Collectors.toSet());
        assertTrue(metricNames.contains("grpc_server_processing_duration_seconds_count"));
        assertTrue(metricNames.contains("grpc_server_processing_duration_seconds_sum"));
        assertTrue(metricNames.contains("grpc_server_processing_duration_seconds_max"));
        assertTrue(metricNames.contains("grpc_client_processing_duration_seconds_count"));
        assertTrue(metricNames.contains("grpc_client_processing_duration_seconds_sum"));
        assertTrue(metricNames.contains("grpc_client_processing_duration_seconds_max"));
    }

    protected void assertTraceIdWithPongService(String expected) {
        String pongTraceId = given()
                .when().get(PONG_ENDPOINT + "/lastTraceId")
                .then().statusCode(HttpStatus.SC_OK).and().extract().asString();

        assertEquals(expected, pongTraceId);
    }
}
