package io.quarkus.ts.opentelemetry.reactive;

import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.using;
import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.usingTimeout;
import static org.hamcrest.Matchers.containsString;

import java.time.Duration;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

/**
 * Test coverage to ensure MicroProfile Telemetry Metrics are available and quarkus.otel.instrument configuration works
 * https://github.com/microprofile/microprofile-telemetry/blob/2.0/spec/src/main/asciidoc/metrics.adoc#required-metrics
 */
@QuarkusScenario
@DisabledOnNative(reason = "To save time and resources needed for building the apps")
public class OpenTelemetryMetricsIT {
    private static final Duration TIME_TO_LOGS_READY = Duration.ofSeconds(15); // metric.export.interval is 10s

    @QuarkusApplication(classes = { PongResource.class })
    static final RestService app = new RestService().withProperties("metrics.properties");

    @QuarkusApplication(classes = { PongResource.class })
    static final RestService appNoJvmMetrics = new RestService().withProperties("metrics.properties")
            .withProperty("quarkus.otel.instrument.jvm-metrics", "false");

    @QuarkusApplication(classes = { PongResource.class })
    static final RestService appNoHttpMetrics = new RestService().withProperties("metrics.properties")
            .withProperty("quarkus.otel.instrument.http-server-metrics", "false");

    @Test
    public void testCpuCountMetricAvailability() {
        metricAvailable(app, "jvm.cpu.count");
    }

    @Test
    public void testJvmClassLoadedMetricAvailability() {
        metricAvailable(app, "jvm.class.loaded");
    }

    @Test
    public void testHttpServerRequestMetricAvailability() {
        invokeHelloEndpoint(app); // invocation needs to happen to have http metric available
        metricAvailable(app, "http.server.request.duration");
    }

    @Test
    public void testHttpServerMetricsDisabled() {
        invokeHelloEndpoint(appNoHttpMetrics);
        metricIsNotAvailable(appNoHttpMetrics, "http.server.request.duration");
        // double check JVM metrics are not disabled as a side effect
        metricAvailable(appNoHttpMetrics, "jvm.class.loaded");
    }

    @Test
    public void testJvmMetricsDisabled() {
        invokeHelloEndpoint(appNoJvmMetrics);
        metricIsNotAvailable(appNoJvmMetrics, "jvm.class.loaded");
        // double check HTTP server metrics are not disabled as a side effect
        metricAvailable(appNoJvmMetrics, "http.server.request.duration");
    }

    private static void metricIsNotAvailable(RestService application, String metricName) {
        // wait TIME_TO_LOGS_READY before checking
        AwaitilityUtils.untilAsserted(() -> application.logs().assertDoesNotContain(metricName),
                using(TIME_TO_LOGS_READY, TIME_TO_LOGS_READY.plusSeconds(1)));
    }

    private static void metricAvailable(RestService application, String metricName) {
        AwaitilityUtils.untilIsTrue(() -> application.getLogs().stream().anyMatch(log -> log.contains(metricName)),
                usingTimeout(TIME_TO_LOGS_READY));
    }

    private static void invokeHelloEndpoint(RestService application) {
        application.given()
                .when().get("/hello")
                .then().statusCode(HttpStatus.SC_OK)
                .body(containsString("pong"));
    }
}
