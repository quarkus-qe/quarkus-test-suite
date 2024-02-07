package io.quarkus.ts.micrometer.prometheus;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.Service;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.QuarkusApplication;

/**
 * The application contains a `PrimeNumberResource` resource that generates a few metrics:
 * - `prime_number_max_{uniqueId}`: max prime number that is found.
 * - `prime_number_test_{uniqueId}`: with information about the calculation of the prime number.
 */
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
public class OpenShiftCustomMetricsIT {

    private static final Logger LOG = Logger.getLogger(OpenShiftCustomMetricsIT.class);

    static final String PRIME_NUMBER_MAX = "prime_number_max_%s";

    static final String PRIME_NUMBER_TEST_COUNT = "prime_number_test_%s_seconds_count";
    static final String PRIME_NUMBER_TEST_MAX = "prime_number_test_%s_seconds_max";
    static final String PRIME_NUMBER_TEST_SUM = "prime_number_test_%s_seconds_sum";

    static final String PROMETHEUS_NAMESPACE = "openshift-user-workload-monitoring";
    static final String PROMETHEUS_POD = "prometheus-user-workload-";
    static final int POD_CARDINALITY = 10;
    static final String PROMETHEUS_CONTAINER = "prometheus";

    static final int ASSERT_PROMETHEUS_TIMEOUT_MINUTES = 5;
    static final int ASSERT_SERVICE_TIMEOUT_MINUTES = 1;
    static final int THREE = 3;
    static final int FOUR = 4;
    static final Integer ANY_VALUE = null;

    @QuarkusApplication
    static RestService app = new RestService()
            /*
             * TODO use https when https://github.com/quarkusio/quarkus/issues/32225 is fixed
             * .withProperty("quarkus.management.ssl.certificate.key-store-file", "META-INF/resources/server.keystore")
             * .withProperty("quarkus.management.ssl.certificate.key-store-password", "password")
             */
            .withProperty("quarkus.management.enabled", "true")
            .onPostStart(OpenShiftCustomMetricsIT::loadServiceMonitor);

    @Inject
    static OpenShiftClient client;

    private String uniqueId;

    @BeforeEach
    public void setup() {
        uniqueId = app.given().get("/uniqueId").then().statusCode(HttpStatus.SC_OK).extract().asString();
    }

    @Test
    public void primeNumberCustomMetricsShouldBeExposed() throws Exception {
        whenCheckPrimeNumber(THREE); // It's prime, so it should set the prime.number.max metric.
        whenCheckPrimeNumber(FOUR); // It's not prime, so It's ignored.

        thenMetricIsExposedInServiceEndpoint(PRIME_NUMBER_MAX, THREE);
        thenMetricIsExposedInServiceEndpoint(PRIME_NUMBER_TEST_COUNT, 1);
        thenMetricIsExposedInServiceEndpoint(PRIME_NUMBER_TEST_MAX, ANY_VALUE);
        thenMetricIsExposedInServiceEndpoint(PRIME_NUMBER_TEST_SUM, ANY_VALUE);

        thenMetricIsExposedInPrometheus(PRIME_NUMBER_MAX, THREE);
        thenMetricIsExposedInPrometheus(PRIME_NUMBER_TEST_COUNT, 1);
        thenMetricIsExposedInPrometheus(PRIME_NUMBER_TEST_MAX, ANY_VALUE);
        thenMetricIsExposedInPrometheus(PRIME_NUMBER_TEST_SUM, ANY_VALUE);
    }

    private void whenCheckPrimeNumber(int number) {
        app.given().get("/check/" + number).then().statusCode(HttpStatus.SC_OK);
    }

    private void thenMetricIsExposedInPrometheus(String name, Integer expected) throws Exception {
        await().ignoreExceptions().atMost(ASSERT_PROMETHEUS_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String output = runPrometheusCommandInPod(PROMETHEUS_POD, primeNumberCustomMetricName(name));

            assertTrue(output.contains("\"status\":\"success\""), "Verify the status was ok");
            assertTrue(output.contains("\"__name__\":\"" + primeNumberCustomMetricName(name) + "\""),
                    "Verify the metrics is found");
            if (expected != null) {
                assertTrue(output.contains("\"" + expected + "\""), "Verify the metrics contains the correct number");
            }

        });
    }

    private String runPrometheusCommandInPod(String podRootName, String metricName) {
        String output = "";
        for (int i = 0; i <= POD_CARDINALITY && output.isEmpty(); i++) {
            String podName = podRootName + i;
            LOG.info("ServiceMonitor expected podName " + podName);
            try {
                output = client.execOnPod(PROMETHEUS_NAMESPACE, podName, PROMETHEUS_CONTAINER, "curl",
                        "http://localhost:9090/api/v1/query?query=" + metricName);

            } catch (Exception e) {
                LOG.warn("Unexpected response from " + podName);
                LOG.warn(e.getMessage());
            }
        }
        return output;
    }

    private void thenMetricIsExposedInServiceEndpoint(String name, Integer expected) {
        await().ignoreExceptions().atMost(ASSERT_SERVICE_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String shouldContain = primeNumberCustomMetricName(name);
            if (expected != null) {
                shouldContain += " " + expected;
            }

            app.management().get("/q/metrics").then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(containsString(shouldContain));
        });
    }

    private String primeNumberCustomMetricName(String metricName) {
        return String.format(metricName, uniqueId);
    }

    private static void loadServiceMonitor(Service app) {
        client.apply(Paths.get("target/test-classes/service-monitor.yaml"));
    }
}
