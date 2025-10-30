package io.quarkus.ts.monitoring.micrometeropentelemetry.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.test.utils.JavaUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractMicrometerOpenTelemetryBridgeIT {

    @LookupService
    static RestService app;

    protected abstract boolean useBasicAuth();

    protected abstract String getLokiUrl();

    protected abstract String getPrometheusUrl();

    protected abstract String getTempoUrl();

    @Test
    void testOpenTelemetryTracing() {
        String traceId = app.given().get("/traces/trace-id").then().statusCode(200).body(notNullValue()).extract().asString();
        AwaitilityUtils.untilAsserted(() -> queryTempoByTraceId(traceId)
                .body("trace.resourceSpans.size()", greaterThanOrEqualTo(1))
                .body("trace.resourceSpans.flatten().resource.attributes.flatten().find { it.key == 'service.name' }.value.stringValue",
                        equalTo("app"))
                .body("trace.resourceSpans.flatten().scopeSpans.flatten().spans.flatten().attributes?.flatten().find { it.key == 'url.path' }.value.stringValue",
                        equalTo("/traces/trace-id")));
    }

    @Test
    void testOpenTelemetryLogging() {
        Instant now = Instant.now();
        app.given().get("/logging").then().statusCode(200).body(is("This is logging resource"));
        AwaitilityUtils.untilAsserted(() -> queryLoki(now)
                .body("data.result.values?.flatten().flatten()", hasItems(
                        "This is an error 2",
                        "This is an error 1",
                        "This is a warning 2",
                        "This is a warning 1",
                        "This is a debug message 2",
                        "This is a debug message 1",
                        "This is info message 2",
                        "This is info message 1")));
    }

    @Test
    void testOpenTelemetryMetrics() {
        callMetricsEndpoint();
        callMetricsEndpoint();
        callMetricsEndpoint();
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("hello_roll_dice_points_count")
                .body("data.result.flatten().metric.service_name", hasItems("app"))
                .body("data.result.flatten().value.flatten()", hasItems("3")));
        queryPrometheusAndWaitForMetrics("hello_roll_dice_points_bucket")
                // we specified 7 explicit bucket boundaries advice
                .body("data.result.flatten().metric.size()", Matchers.greaterThanOrEqualTo(7));
    }

    @Test
    void testCustomMicrometerMetrics() {
        double increase = 525.6;
        app.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("increase", increase)
                .get("/micrometer/counter")
                .then().statusCode(200)
                .body(is("Counter increased by " + increase));
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("count_me_beans_total").body(
                "data.result.find { it.metric.__name__ == 'count_me_beans_total' }.value.get(1)",
                is(String.valueOf(increase))));
        double secondIncrease = 111.5;
        app.given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("increase", secondIncrease)
                .get("/micrometer/counter")
                .then().statusCode(200)
                .body(is("Counter increased by " + secondIncrease));
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("count_me_beans_total").body(
                "data.result.find { it.metric.__name__ == 'count_me_beans_total' }.value.get(1)",
                is(String.valueOf(increase + secondIncrease))));
        // test the metric tag
        queryPrometheusAndWaitForMetrics("count_me_beans_total").body("data.result.metric.region", everyItem(is("test")));
    }

    @Test
    void testMicrometerDistributionSummary() {
        byte[] payload = new byte[55];
        app.given()
                .body(payload)
                .accept(ContentType.JSON)
                .post("/micrometer/distribution-summary")
                .then().statusCode(200)
                .body(is(Double.valueOf(payload.length).toString()));
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("my_bytes_received_max")
                .body("data.result.value.flatten()", hasItems(String.valueOf(payload.length))));
    }

    @Test
    void testSemanticConventionDifferences() {
        app.given().get("/micrometer/timed").then().statusCode(200).body(is("hello from timed method"));
        app.given().get("/micrometer/counted-failures").then().statusCode(200).body(is("hello from counted method"));
        app.given().queryParam("fail", true).get("/micrometer/counted-failures").then().statusCode(500);
        app.given().get("/micrometer/counted-failures").then().statusCode(200).body(is("hello from counted method"));

        AwaitilityUtils.untilAsserted(() -> {
            double sum = queryPrometheusAndWaitForMetrics("my_timed_method_milliseconds_sum").extract().jsonPath()
                    .getDouble("data.result.value.flatten().get(1)");
            Assertions.assertTrue(sum >= 1300, "The 'my_timed_method_milliseconds_sum' metric should "
                    + "record at least 1300 milliseconds as the method was executed for 1300 milliseconds, but was "
                    + sum);
            queryPrometheusAndWaitForMetrics("my_timed_method_milliseconds_count").body("data.result.value.flatten()",
                    hasItems("1"));
        });
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("counted_total")
                .body("data.result.value.flatten()", hasItems("1")));

        // basically this only checks that these metrics are there as declared
        var body = queryPrometheusAndWaitForMetrics("{__name__=~\".+\"}").extract().body().asPrettyString();

        // Micrometer (gauge): http.server.active.requests
        // Prometheus format (gauge): http_server_active_requests
        // Bridge format (double gauge): http.server.active.requests
        assertBodyContains(body, "http_server_active_requests");
        // Micrometer: http.server.requests
        // Prometheus format: http_server_requests_seconds_count, http_server_requests_seconds_sum,
        // http_server_requests_seconds_max
        // Bridge format: http.server.requests, http.server.requests.max
        assertBodyContains(body, "http_server_requests_milliseconds_count");
        assertBodyContains(body, "http_server_requests_milliseconds_sum");
        assertBodyContains(body, "http_server_requests_max_milliseconds");
        // Micrometer: http.server.bytes.read
        // Prometheus format: http_server_bytes_read_count, http_server_bytes_read_sum, http_server_bytes_read_max
        // Bridge format: http.server.bytes.read, http.server.bytes.read.max
        assertBodyContains(body, "http_server_bytes_read_max");
        // Micrometer: http.server.connections
        // Prometheus format: http_server_connections_seconds_active_count, http_server_connections_seconds_duration_sum,
        // http_server_connections_seconds_max
        // Bridge format: http.server.connections.active, http.server.connections.duration
        assertBodyContains(body, "http_server_connections_active");
        assertBodyContains(body, "http_server_connections_duration_milliseconds");
        // Micrometer: jvm.threads.live
        // Prometheus format: jvm_threads_live_threads
        // Bridge format: jvm.threads.live
        assertBodyContains(body, "jvm_threads_live");
        // Micrometer: jvm.threads.started
        // Prometheus format: jvm_threads_started_threads_total
        // Bridge format: jvm.threads.started
        assertBodyContains(body, "jvm_threads_started_total");
        // Micrometer: jvm.threads.daemon
        // Prometheus format: jvm_threads_daemon_threads
        // Bridge format: jvm.threads.daemon
        assertBodyContains(body, "jvm_threads_daemon");
        // Micrometer: jvm.threads.peak
        // Prometheus format: jvm_threads_peak_threads
        // Bridge format: jvm.threads.peak
        assertBodyContains(body, "jvm_threads_peak");
        // Micrometer: jvm.threads.states
        // Prometheus format: jvm_threads_states_threads
        // Bridge format: jvm.threads.states
        assertBodyContains(body, "jvm_threads_states");
        // Micrometer: http.server.bytes.read
        // Prometheus format: http_server_bytes_read_count, http_server_bytes_read_sum , http_server_bytes_read_max
        // Bridge format: http.server.bytes.read, http.server.bytes.read.max
        byte[] payload = new byte[77];
        app.given()
                .body(payload)
                .post("/micrometer/echo-bytes")
                .then().statusCode(200)
                .body(Matchers.notNullValue());
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("http_server_bytes_read_sum")
                .body("data.result.value.flatten()", hasItems(String.valueOf(payload.length))));
        // one call to the "echo-bytes", but this can change if order of methods changed or additions as it is
        // a global metric
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("http_server_bytes_read_count")
                .body("data.result.value.flatten()", hasItems("1")));
    }

    @Test
    void testAutomaticallyGeneratedMicrometerMetrics_NettyBinder() {
        // Netty memory management metrics are enabled by default,
        // but we disabled the binder to test that the Micrometer config works
        queryPrometheus("netty_allocator_memory_used", 0)
                .body("data?.result?.flatten().size()", is(0));
        queryPrometheus("netty_allocator_memory_pinned", 0)
                .body("data?.result?.flatten().size()", is(0));
    }

    @Test
    void testAutomaticallyGeneratedMicrometerMetrics_HttpServerBinder() {
        app.given()
                .get("/micrometer/song")
                .then()
                .statusCode(HttpStatus.SC_OK);
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("http_server_requests_milliseconds_count")
                .body(
                        "data.result.metric.find { it.uri == '/micrometer/song' && it.status == '200' }.service_name",
                        is("app"))
                .body(
                        "data.result.metric.find { it.uri == '/micrometer/song' && it.status == '400' }.service_name",
                        nullValue()));
        app.given()
                .header("bad-request", true)
                .get("/micrometer/song")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
        AwaitilityUtils.untilAsserted(() -> queryPrometheusAndWaitForMetrics("http_server_requests_milliseconds_count").body(
                "data.result.metric.find { it.uri == '/micrometer/song' && it.status == '400' }.service_name", is("app")));
    }

    @DisabledOnNative(reason = "JVM binder is for JVM verification only")
    @Test
    void testAutomaticallyGeneratedMicrometerMetrics_JvmBinder() {
        // Semeru JDK has different metrics than OpenJDK, when checking metrics we need to get different one
        // for openjdk, the metaspace can hardly be 0, so just test that some bytes are measured
        String metricsId = JavaUtils.isRunningSemeruJdk() ? "JIT code cache" : "Metaspace";

        long metaspaceValue = queryPrometheusAndWaitForMetrics("jvm_memory_used_bytes").extract().jsonPath().getLong(
                "data.result.find { it.metric.id == '" + metricsId + "' }.value.get(1)");
        Assertions.assertTrue(metaspaceValue > 0);
    }

    private static void callMetricsEndpoint() {
        long roll = app.given().get("/metrics").then().statusCode(HttpStatus.SC_OK).extract().as(Long.class);
        assertTrue(roll >= 1, "Roll value should be equal or greater than 1, got " + roll);
    }

    private ValidatableResponse queryLoki(Instant start) {
        String startTimestamp = String.valueOf(start.getEpochSecond());
        String endTimestamp = String.valueOf(Instant.now().plusSeconds(20).getEpochSecond());
        return given().when()
                .queryParam("query", "{service_name=\"app\"}")
                .queryParam("limit", 500)
                .queryParam("direction", "forward")
                .queryParam("start", startTimestamp)
                .queryParam("end", endTimestamp)
                .get(getLokiUrl() + "/loki/api/v1/query_range")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"));
    }

    private ValidatableResponse queryTempoByTraceId(String traceId) {
        return given().when()
                .get(getTempoUrl() + "/api/v2/traces/" + traceId)
                .then()
                .statusCode(200);
    }

    private ValidatableResponse queryPrometheusAndWaitForMetrics(String metric) {
        AwaitilityUtils.untilAsserted(() -> queryPrometheus(metric, 1));
        return queryPrometheus(metric, 1);
    }

    private ValidatableResponse queryPrometheus(String metric, int minSize) {
        return given()
                .queryParam("query", metric)
                .get(getPrometheusUrl() + "/api/v1/query")
                .then().statusCode(200)
                .body("status", is("success"))
                .body("data?.result?.flatten().size()", greaterThanOrEqualTo(minSize));
    }

    private static void assertBodyContains(String body, String metric) {
        Assertions.assertTrue(body.contains(metric),
                "Metric '%s' is not present in the list of all metrics '%s'".formatted(metric, body));
    }

    private RequestSpecification given() {
        if (useBasicAuth()) {
            return RestAssured.given().auth().preemptive().basic("admin", "admin");
        }
        return RestAssured.given();
    }
}
