package io.quarkus.ts.micrometer.prometheus.kafka;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;

public abstract class BaseOpenShiftAlertEventsIT {

    static final String PATH = "/monitor/stream";
    static final String KAFKA_CONSUMER_COUNT_METRIC = "kafka_consumer_response_total";
    static final String KAFKA_PRODUCER_COUNT_METRIC = "kafka_producer_response_total";
    static final int WAIT_FOR_ALERTS_COUNT = 1;

    static final String PROMETHEUS_NAMESPACE = "openshift-user-workload-monitoring";
    static final String PROMETHEUS_POD = "prometheus-user-workload-0";
    static final String PROMETHEUS_CONTAINER = "prometheus";
    static final int ASSERT_PROMETHEUS_TIMEOUT_MINUTES = 5;
    static final int ASSERT_SERVICE_TIMEOUT_MINUTES = 1;

    static final int TIMEOUT_SEC = 25;

    @Inject
    static OpenShiftClient client;

    private List<String> receive = new CopyOnWriteArrayList<>();

    @Test
    public void testAlertMonitorEventStream() throws Exception {
        whenWaitUntilReceiveSomeAlerts();
        thenExpectedAlertsHaveBeenConsumed();
        thenKafkaProducerMetricsAreFound();
        thenKafkaConsumerMetricsAreFound();
    }

    protected abstract RestService getApp();

    private void whenWaitUntilReceiveSomeAlerts() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(WAIT_FOR_ALERTS_COUNT);

        WebTarget target = ClientBuilder.newClient().target(getApp().getHost() + ":" + getApp().getPort() + PATH);
        SseEventSource source = SseEventSource.target(target).build();
        source.register(inboundSseEvent -> {
            receive.add(inboundSseEvent.readData(String.class, MediaType.APPLICATION_JSON_TYPE));
            latch.countDown();
        });

        source.open();
        latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        source.close();
    }

    private void thenExpectedAlertsHaveBeenConsumed() {
        assertEquals(WAIT_FOR_ALERTS_COUNT, receive.size(), "Unexpected number of alerts consumed");
    }

    private void thenKafkaProducerMetricsAreFound() throws Exception {
        thenMetricIsExposedInServiceEndpoint(KAFKA_PRODUCER_COUNT_METRIC, greater(0));
        thenMetricIsExposedInPrometheus(KAFKA_PRODUCER_COUNT_METRIC, any());
    }

    private void thenKafkaConsumerMetricsAreFound() throws Exception {
        thenMetricIsExposedInServiceEndpoint(KAFKA_CONSUMER_COUNT_METRIC, greater(0));
        thenMetricIsExposedInPrometheus(KAFKA_CONSUMER_COUNT_METRIC, any());
    }

    private void thenMetricIsExposedInPrometheus(String name, Predicate<String> valueMatcher) throws Exception {
        await().ignoreExceptions().atMost(ASSERT_PROMETHEUS_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String output = client.execOnPod(PROMETHEUS_NAMESPACE, PROMETHEUS_POD, PROMETHEUS_CONTAINER, "curl",
                    "http://localhost:9090/api/v1/query?query=" + name);

            assertTrue(output.contains("\"status\":\"success\""), "Verify the status was ok");
            assertTrue(output.contains("\"__name__\":\"" + name + "\""), "Verify the metrics is found");
            assertTrue(valueMatcher.test(output), "Verify the metrics contains the correct number");
        });
    }

    private void thenMetricIsExposedInServiceEndpoint(String name, Predicate<Double> valueMatcher) {
        await().ignoreExceptions().atMost(ASSERT_SERVICE_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String response = getApp().given().get("/q/metrics").then()
                    .statusCode(HttpStatus.SC_OK)
                    .extract().asString();

            boolean matches = false;
            for (String line : response.split("[\r\n]+")) {
                if (line.startsWith(name)) {
                    Double value = extractValueFromMetric(line);
                    assertTrue(valueMatcher.test(value), "Metric value is not expected. Found: " + value);
                    matches = true;
                    break;
                }
            }

            assertTrue(matches, "Metric " + name + " not found in " + response);
        });
    }

    private <T> Predicate<T> any() {
        return actual -> true;
    }

    private Predicate<Double> greater(double expected) {
        return actual -> actual > expected;
    }

    private Double extractValueFromMetric(String line) {
        return Double.parseDouble(line.substring(line.lastIndexOf(" ")));
    }

    protected static void loadServiceMonitor(Service app) {
        client.apply(Paths.get("target/test-classes/service-monitor.yaml"));
    }
}
