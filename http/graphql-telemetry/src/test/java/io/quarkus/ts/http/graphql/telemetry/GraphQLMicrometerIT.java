package io.quarkus.ts.http.graphql.telemetry;

import static io.quarkus.ts.http.graphql.telemetry.utils.GraphQLUtils.createQuery;
import static io.quarkus.ts.http.graphql.telemetry.utils.GraphQLUtils.sendQuery;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

@DisabledForJreRange(max = JRE.JAVA_20, disabledReason = "VTs supported for Java 21+")
@Tag("QUARKUS-6521")
@QuarkusScenario
@DisabledOnSemeruJdk(reason = "Flight Recorder is not supported on IBM Semeru Runtime")
public class GraphQLMicrometerIT {

    private static final String JVM_THREADS_VIRTUAL = "jvm_threads_virtual_";
    private static final String PINNED_SECONDS_COUNT = JVM_THREADS_VIRTUAL + "pinned_seconds_count";
    private static final String PINNED_SECONDS_MAX = JVM_THREADS_VIRTUAL + "pinned_seconds_max";
    private static final String PINNED_SECONDS_SUM = JVM_THREADS_VIRTUAL + "pinned_seconds_sum";
    private static final String SUBMIT_FAILED_TOTAL = JVM_THREADS_VIRTUAL + "submit_failed_total";

    @QuarkusApplication(dependencies = {
            @Dependency(artifactId = "quarkus-micrometer-registry-prometheus")
    })
    static final RestService app = new RestService();

    @Test
    void verifyVirtualThreadsMetrics() throws InterruptedException {
        // run multiple concurrent requests to put the application into a test
        CountDownLatch done = new CountDownLatch(100);
        IntStream.range(0, 100).forEach(i -> new Thread(() -> {
            Response classic = sendQuery(createQuery("friend_vt(name:\"Aristotle\"){name}"));
            Assertions.assertEquals("Plato", classic.jsonPath().getString("data.friend_vt.name"));
            Response reactive = sendQuery(createQuery("friend_vt(name:\"Plato\"){name}"));
            Assertions.assertEquals("Aristotle", reactive.jsonPath().getString("data.friend_vt.name"));
            done.countDown();
        }).start());
        // now verify that all the requests were executed on a virtual thread without pinning
        boolean allRequestsFinished = done.await(15, TimeUnit.SECONDS);
        assertTrue(allRequestsFinished, "At least one GraphQL endpoint call did not succeed");
        var virtualThreadsMetrics = getVirtualThreadsMetrics();
        assertThat(virtualThreadsMetrics)
                .containsKey(PINNED_SECONDS_COUNT)
                .containsKey(PINNED_SECONDS_MAX)
                .containsKey(PINNED_SECONDS_SUM)
                .containsKey(SUBMIT_FAILED_TOTAL)
                .extractingByKeys(PINNED_SECONDS_COUNT, PINNED_SECONDS_MAX, PINNED_SECONDS_SUM, SUBMIT_FAILED_TOTAL)
                .allMatch(metricValue -> 0.0 == metricValue);
    }

    private static Map<String, Double> getVirtualThreadsMetrics() {
        return getMetrics()
                .extract().asString().lines()
                .filter(l -> l.startsWith(JVM_THREADS_VIRTUAL))
                .map(String::strip)
                .map(l -> l.split(" "))
                .collect(Collectors.toMap(l -> l[0], l -> Double.parseDouble(l[1])));
    }

    private static ValidatableResponse getMetrics() {
        return given()
                .get("/q/metrics")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }
}
