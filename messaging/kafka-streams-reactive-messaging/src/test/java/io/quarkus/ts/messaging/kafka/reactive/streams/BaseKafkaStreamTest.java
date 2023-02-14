package io.quarkus.ts.messaging.kafka.reactive.streams;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.SseEventSource;

import org.junit.jupiter.api.Test;

public abstract class BaseKafkaStreamTest {

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

    protected abstract String getAppUrl();

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
}
