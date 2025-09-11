package io.quarkus.ts.opentelemetry.reactive;

import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.using;
import static io.quarkus.test.utils.AwaitilityUtils.AwaitilitySettings.usingTimeout;
import static org.hamcrest.Matchers.containsString;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;
import io.quarkus.ts.opentelemetry.reactive.metrics.gc.RunGarbageCollectorTrigger;

/**
 * Test coverage to ensure MicroProfile Telemetry Metrics are available and quarkus.otel.instrument configuration works
 * https://github.com/microprofile/microprofile-telemetry/blob/2.0/spec/src/main/asciidoc/metrics.adoc#required-metrics
 */
@QuarkusScenario
@DisabledOnNative(reason = "To save time and resources needed for building the apps")
public class OpenTelemetryMetricsIT {
    private static final Duration TIME_TO_LOGS_READY = Duration.ofSeconds(15); // metric.export.interval is 10s
    /**
     * Pattern to extract value from "value=24,".
     */
    private static final Pattern VALUE_PATTERN = Pattern.compile("value=(\\d+),");
    /**
     * Pattern to extract epoch nanos
     * from "ImmutableLongPointData{startEpochNanos=1757528051068455008, epochNanos=1757528061070604949".
     */
    private static final Pattern EPOCH_NANOS_PATTERN = Pattern.compile("startEpochNanos=(\\d+),\\s*epochNanos=(\\d+)");
    /**
     * Pattern to extract epoch nanos
     * from "ImmutableHistogramPointData{getStartEpochNanos=1757528077614712112, getEpochNanos=1757528090993409098".
     */
    private static final Pattern GET_EPOCH_NANOS_PATTERN = Pattern
            .compile("getStartEpochNanos=(\\d+),\\s*getEpochNanos=(\\d+)");

    @QuarkusApplication(classes = { PongResource.class, RunGarbageCollectorTrigger.class })
    static final RestService app = new RestService().withProperties("metrics.properties");

    @QuarkusApplication(classes = { PongResource.class })
    static final RestService appNoJvmMetrics = new RestService().withProperties("metrics.properties")
            .withProperty("quarkus.otel.instrument.jvm-metrics", "false");

    @QuarkusApplication(classes = { PongResource.class })
    static final RestService appNoHttpMetrics = new RestService().withProperties("metrics.properties")
            .withProperty("quarkus.otel.instrument.http-server-metrics", "false");

    @Tag("https://github.com/quarkusio/quarkus/issues/46535")
    @Test
    public void testCpuMetricsAvailability() {
        metricAvailable(app, "jvm.cpu.count");
        verifyNoDuplicateMetrics("jvm.cpu.count");
        metricAvailable(app, "jvm.cpu.time");
        verifyNoDuplicateMetrics("jvm.cpu.time");
        metricAvailable(app, "jvm.cpu.recent_utilization");
        verifyNoDuplicateMetrics("jvm.cpu.recent_utilization");
        metricAvailable(app, "jvm.cpu.limit");
        verifyNoDuplicateMetrics("jvm.cpu.limit");
        metricAvailable(app, "jvm.cpu.longlock");
        verifyNoDuplicateMetrics("jvm.cpu.longlock");
        metricAvailable(app, "jvm.cpu.context_switch");
        verifyNoDuplicateMetrics("jvm.cpu.context_switch");
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/46535")
    @Test
    public void testJvmClassMetricsAvailability() {
        metricAvailable(app, "jvm.class.loaded");
        verifyNoDuplicateMetrics("jvm.class.loaded");
        metricAvailable(app, "jvm.class.unloaded");
        verifyNoDuplicateMetrics("jvm.class.unloaded");
        metricAvailable(app, "jvm.class.count");
        verifyNoDuplicateMetrics("jvm.class.count");
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/46535")
    @Test
    public void testJvmMemoryMetricsAvailability() {
        metricAvailable(app, "jvm.memory.used");
        verifyAtLeastOneMetricValueGreaterThan0("jvm.memory.used");
        verifyNoDuplicateMetrics("jvm.memory.used");
        metricAvailable(app, "jvm.memory.committed");
        verifyAtLeastOneMetricValueGreaterThan0("jvm.memory.committed");
        verifyNoDuplicateMetrics("jvm.memory.committed");
        metricAvailable(app, "jvm.memory.limit");
        verifyAtLeastOneMetricValueGreaterThan0("jvm.memory.limit");
        verifyNoDuplicateMetrics("jvm.memory.limit");
        metricAvailable(app, "jvm.memory.used_after_last_gc");
        verifyAtLeastOneMetricValueGreaterThan0("jvm.memory.used_after_last_gc");
        verifyNoDuplicateMetrics("jvm.memory.used_after_last_gc");
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/46535")
    @Test
    public void testJvmThreadCountMetricAvailability() {
        metricAvailable(app, "jvm.thread.count");
        verifyNoDuplicateMetrics("jvm.thread.count");
    }

    @Tag("https://github.com/quarkusio/quarkus/issues/46535")
    @Test
    public void testJvmGarbageCollectionDurationMetricAvailability() {
        metricAvailable(app, "jvm.gc.duration");
        verifyNoDuplicateMetrics("jvm.gc.duration");
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

    private static void verifyNoDuplicateMetrics(String metricName) {
        List<String> allMetricEpochNanos = extractMetricEpochNanos(metricName);
        long metricEpochNanos = allMetricEpochNanos.size();
        long distinctEpochNanos = allMetricEpochNanos.stream().distinct().count();
        Assertions.assertEquals(metricEpochNanos, distinctEpochNanos,
                "The " + metricName + " metric has been reported more than once for the same time period");
    }

    private static List<String> extractMetricEpochNanos(String metricName) {
        return logLinesWithMetric(metricName)
                .map(OpenTelemetryMetricsIT::extractLogLineEpochNanos)
                .toList();
    }

    private static Stream<String> logLinesWithMetric(String metricName) {
        // we must add comma because some metric names are prefix for other metrics
        return app.getLogs().stream().filter(log -> log.contains(metricName + ","));
    }

    private static String extractLogLineEpochNanos(String logLine) {
        Pattern pattern = EPOCH_NANOS_PATTERN;
        Matcher matcher = pattern.matcher(logLine);

        if (matcher.find()) {
            String startEpoch = matcher.group(1);
            String epoch = matcher.group(2);
            return "startEpochNanos=" + startEpoch + ", epochNanos=" + epoch;
        }

        pattern = GET_EPOCH_NANOS_PATTERN;
        matcher = pattern.matcher(logLine);

        if (matcher.find()) {
            String startEpoch = matcher.group(1);
            String epoch = matcher.group(2);
            return "getStartEpochNanos=" + startEpoch + ", getEpochNanos=" + epoch;
        }

        // this means we need to adjust our test as either the pattern has changed or there are some log lines without
        // epoch nanos
        throw new IllegalStateException("Log message does not contain regular expression '%s': %s".formatted(pattern, logLine));
    }

    /**
     * The <a href="https://github.com/quarkusio/quarkus/issues/46535">reported issue</a> showing that some memory
     * metrics can be reported with all zero values. Here we check that all the reported values are not zero.
     * If this method turns out to be flaky, please consider there may be legit reasons why the value is zero.
     *
     * @param metricName metric name
     */
    private static void verifyAtLeastOneMetricValueGreaterThan0(String metricName) {
        logLinesWithMetric(metricName).forEach(logLine -> {
            Set<Long> values = extractValues(logLine);
            Assertions.assertFalse(values.isEmpty(),
                    "The " + metricName + " metric has been reported without any value: " + logLine);
            boolean anyValueGreaterThanZero = values.stream().anyMatch(value -> value > 0);
            Assertions.assertTrue(anyValueGreaterThanZero,
                    "The metric " + metricName + " has no values greater than zero: " + logLine);
        });
    }

    private static Set<Long> extractValues(String logLine) {
        // value=24,      =>     24
        Set<Long> values = new HashSet<>();
        Matcher matcher = VALUE_PATTERN.matcher(logLine);

        while (matcher.find()) {
            values.add(Long.parseLong(matcher.group(1)));
        }

        return values;
    }
}
