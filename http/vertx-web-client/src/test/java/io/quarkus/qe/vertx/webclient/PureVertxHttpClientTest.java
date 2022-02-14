package io.quarkus.qe.vertx.webclient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;

@QuarkusTest
//TODO: https://github.com/quarkusio/quarkus/issues/23684
@Disabled
public class PureVertxHttpClientTest {

    WebClient httpClient;
    static final int EXPECTED_EVENTS = 25000;
    private static final int TIMEOUT_SEC = 10;

    @BeforeEach
    public void setup() {
        httpClient = WebClient.create(Vertx.vertx(), new WebClientOptions());
    }

    @Test
    public void quarkusTestFipsVertxHttpClient() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(EXPECTED_EVENTS);

        for (int i = 0; i < EXPECTED_EVENTS; i++) {
            httpClient.getAbs("http://localhost:8081" + "/chuck/pong")
                    .expect(ResponsePredicate.status(200))
                    .send().subscribe().with(resp -> done.countDown());
        }

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertEquals(0, done.getCount(), String.format("Missing %d events.", EXPECTED_EVENTS - done.getCount()));
    }
}
