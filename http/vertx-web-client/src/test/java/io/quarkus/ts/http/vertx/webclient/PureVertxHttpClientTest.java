package io.quarkus.ts.http.vertx.webclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;

@QuarkusTest
@Tag("fips-incompatible") // Reported in https://github.com/quarkusio/quarkus/issues/23865
public class PureVertxHttpClientTest {

    @TestHTTPResource
    URL url;

    WebClient httpClient;
    Vertx vertx;
    static final int EXPECTED_EVENTS = 25000;
    private static final int TIMEOUT_SEC = 10;

    @BeforeEach
    public void setup() {
        vertx = Vertx.vertx();
        httpClient = WebClient.create(vertx, new WebClientOptions());
    }

    @Test
    public void quarkusTestFipsVertxHttpClient() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(EXPECTED_EVENTS);

        for (int i = 0; i < EXPECTED_EVENTS; i++) {
            httpClient.getAbs(url.toString() + "/chuck/pong")
                    .expect(ResponsePredicate.status(200))
                    .send().subscribe().with(resp -> done.countDown());
        }

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertEquals(0, done.getCount(), String.format("Missing %d events.", EXPECTED_EVENTS - done.getCount()));
    }

    @AfterEach
    public void cleanUp() {
        if (httpClient != null)
            httpClient.close();

        if (vertx != null)
            vertx.close();
    }
}
