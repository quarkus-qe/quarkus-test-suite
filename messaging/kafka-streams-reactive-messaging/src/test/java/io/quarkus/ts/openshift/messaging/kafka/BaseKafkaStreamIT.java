package io.quarkus.ts.openshift.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;

public abstract class BaseKafkaStreamIT {

    private static final int TIMEOUT_SEC = 25;
    private static final int EVENTS_AMOUNT = 1;

    private String endpoint;
    private Client client = ClientBuilder.newClient();
    private AtomicInteger receivedEvents = new AtomicInteger(0);

    @Test
    public void testAlertMonitorEventStream() throws InterruptedException {
        givenAnApplicationEndpoint("/monitor/stream");
        whenRequestSomeEvents();
        thenVerifyAllEventsArrived();
    }

    protected abstract RestService getApp();

    private void givenAnApplicationEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private void whenRequestSomeEvents() throws InterruptedException {
        WebTarget target = client.target(getAppUrl() + endpoint);
        final CountDownLatch latch = new CountDownLatch(EVENTS_AMOUNT);

        SseEventSource source = SseEventSource.target(target).build();
        source.register(inboundSseEvent -> {
            receivedEvents.incrementAndGet();
            latch.countDown();
        });

        source.open();
        latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        source.close();
    }

    private void thenVerifyAllEventsArrived() {
        assertTrue(receivedEvents.get() >= EVENTS_AMOUNT,
                "Not all expected kafka events has been consumed. Got: " + receivedEvents.get());
    }

    private String getAppUrl() {
        return getApp().getHost() + ":" + getApp().getPort();
    }
}
