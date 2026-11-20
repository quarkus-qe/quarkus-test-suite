package io.quarkus.ts.http.graphql.telemetry;

import static io.quarkus.ts.http.graphql.telemetry.utils.GraphQLUtils.createQuery;
import static io.quarkus.ts.http.graphql.telemetry.utils.GraphQLUtils.sendQuery;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.JaegerService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.JaegerContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class GraphQLTelemetryIT {

    @JaegerContainer(useOtlpCollector = true)
    static JaegerService jaeger = new JaegerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.otel.exporter.otlp.endpoint", jaeger::getCollectorUrl)
            .withProperty("quarkus.otel.traces.sampler.arg", "1.0d");

    @Test
    void verifyTelemetry() {
        // Jaeger v3 API requires a time window
        String startMin = Instant.now().minus(1, ChronoUnit.HOURS).toString();
        String startMax = Instant.now().plus(1, ChronoUnit.HOURS).toString();

        Response classic = sendQuery(createQuery("friend(name:\"Aristotle\"){name}"));
        Response reactive = sendQuery(createQuery("friend_r(name:\"Aristotle\"){name}"));

        Assertions.assertEquals("Plato", classic.jsonPath().getString("data.friend.name"));
        Assertions.assertEquals("Plato", reactive.jsonPath().getString("data.friend_r.name"));

        await().atMost(1, TimeUnit.MINUTES).pollInterval(Duration.ofSeconds(10)).untilAsserted(() -> {
            String operation = "POST /graphql";
            Response traces = given().when()
                    .queryParam("query.service_name", "graphql-telemetry")
                    .queryParam("query.operation_name", operation)
                    .queryParam("query.start_time_min", startMin)
                    .queryParam("query.start_time_max", startMax)
                    .get(jaeger.getTraceV3Url());
            Assertions.assertEquals(HttpURLConnection.HTTP_OK, traces.statusCode());

            List<Map<String, Object>> spans = traces.jsonPath()
                    .getList("result.resourceSpans.scopeSpans.spans.flatten()");
            Map<Object, List<Map<String, Object>>> spansByTrace = spans.stream()
                    .collect(Collectors.groupingBy(span -> span.get("traceId")));

            // one trace for the classic query, one for the reactive one
            Assertions.assertEquals(2, spansByTrace.size(), "Unexpected traces: " + spansByTrace);
            for (List<Map<String, Object>> traceSpans : spansByTrace.values()) {
                Assertions.assertEquals(2, traceSpans.size(), "Unexpected spans: " + traceSpans);
                Map<String, Object> httpSpan = findSpan(traceSpans, operation);
                Map<String, Object> graphQLSpan = findSpan(traceSpans, "GraphQL");
                // the GraphQL span is a child of the HTTP span
                Assertions.assertEquals(httpSpan.get("spanId"), graphQLSpan.get("parentSpanId"));
            }
        });
    }

    private static Map<String, Object> findSpan(List<Map<String, Object>> spans, String name) {
        return spans.stream()
                .filter(span -> name.equals(span.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No span named '" + name + "' in " + spans));
    }
}
