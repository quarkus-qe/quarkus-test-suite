package io.quarkus.ts.http.vertx.webclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;

@QuarkusTest
public class PureVertxHttpClientTest {

    @TestHTTPResource
    URL url;

    @Inject
    Vertx vertx;
    private static final int EXPECTED_EVENTS = 25000;
    private static final int TIMEOUT_SEC = 10;

    @Test
    public void quarkusTestFipsVertxHttpClient() throws InterruptedException {
        var webClient = WebClient.create(vertx, new WebClientOptions());
        try {
            CountDownLatch done = new CountDownLatch(EXPECTED_EVENTS);

            for (int i = 0; i < EXPECTED_EVENTS; i++) {
                webClient.getAbs(url.toString() + "/chuck/pong")
                        .expect(ResponsePredicate.status(200))
                        .send().subscribe().with(resp -> done.countDown());
            }

            done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
            assertEquals(0, done.getCount(), String.format("Missing %d events.", EXPECTED_EVENTS - done.getCount()));
        } finally {
            webClient.close();
        }
    }

}
