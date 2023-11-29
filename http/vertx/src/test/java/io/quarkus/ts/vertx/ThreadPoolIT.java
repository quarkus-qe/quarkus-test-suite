package io.quarkus.ts.vertx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.vertx.AbstractVertxIT.Metric;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.http.HttpClientRequest;

@DisabledOnNative // thread pool test is not relevant for a native mode
@QuarkusScenario
public class ThreadPoolIT {

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.thread-pool.core-threads", "1") // set explicitly in case default changes
            .withProperty("quarkus.thread-pool.max-threads", "1")
            .withProperty("quarkus.thread-pool.growth-resistance", "1f")
            .withProperty("quarkus.thread-pool.queue-size", "0");

    @Tag("QUARKUS-3535")
    @Tag("QUARKUS-3536")
    @Test
    public void testRejectedPoolSubmissionMetric() {
        float numberOfRejected = numberOfRejectedRequests();
        assertEquals(0, numberOfRejected, "Excepted no rejected requests, got: " + numberOfRejected);
        long serviceUnavailableCount = callHelloWorld(1);
        assertEquals(0, serviceUnavailableCount, "Quarkus responded with HTTP status 503, "
                + "but there is no reason to reject single request");
        numberOfRejected = numberOfRejectedRequests();
        assertEquals(0, numberOfRejected, "Excepted no rejected requests, got: " + numberOfRejected);
        serviceUnavailableCount = callHelloWorld(10);
        numberOfRejected = numberOfRejectedRequests();
        assertTrue(numberOfRejected > 0, "At least one request was expected to be rejected, "
                + "this is either bug or Quarkus is super effective in handling of incoming requests");
        assertTrue(serviceUnavailableCount > 0, "Quarkus rejected " + numberOfRejected + " thread pool"
                + " submissions but did not respond once with 503");
    }

    private static long callHelloWorld(int howManyTimes) {
        var vertx = Vertx.vertx();
        var httpClient = vertx.createHttpClient();
        var clientOptions = new RequestOptions();
        clientOptions.setTimeout(300);
        clientOptions.setMethod(HttpMethod.GET);
        var uri = app.getURI(Protocol.HTTP);
        clientOptions.setHost(uri.getHost());
        clientOptions.setPort(uri.getPort());
        clientOptions.setURI("/hello/blocking");
        try {
            var joinBuilder = Uni.join().<Integer> builder();
            for (int i = 0; i < howManyTimes; i++) {
                var request = httpClient
                        .request(clientOptions)
                        .flatMap(HttpClientRequest::send)
                        .flatMap(s -> s.end().map(v -> s.statusCode()))
                        // when thread pool is exhausted and queue is full we can get closed connection
                        // but by then we already will have rejected count greater than zero
                        .onFailure().recoverWithItem(-1);
                joinBuilder.add(request);
            }
            // return number of times response got rejected because of the 'RejectedExecutionException'
            return joinBuilder
                    .joinAll()
                    .usingConcurrencyOf(howManyTimes)
                    .andFailFast()
                    .await()
                    .indefinitely()
                    .stream()
                    .filter(s -> s == 503)
                    .count();
        } finally {
            httpClient.close().await().indefinitely();
            vertx.close().await().indefinitely();
        }
    }

    private static float numberOfRejectedRequests() {
        String body = app
                .given()
                .get("/q/metrics")
                .then()
                .statusCode(200)
                .extract()
                .asString();
        return Arrays.stream(body.split("\n"))
                .filter(line -> !line.startsWith("#"))
                .map(Metric::new)
                .filter(m -> "worker_pool_rejected_total".equals(m.getName()))
                // there are 2 'worker_pool_rejected_total' metrics
                // - vert.x-internal-blocking
                // - vert.x-worker-thread
                // we test worker thread as that one is used by users
                .filter(m -> m.getObject() != null && m.getObject().contains("vert.x-worker-thread"))
                .findFirst()
                .map(Metric::getValue)
                .map(Float::parseFloat)
                .orElseThrow(() -> new IllegalStateException("Metric 'worker_pool_rejected_total' was not found"));
    }
}
