package io.quarkus.ts.openshift.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSource;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.openshift.messaging.kafka.aggregator.model.LoginAggregation;

public abstract class BaseKafkaStreamIT {

    private static final int TIMEOUT_SEC = 25;
    private static final int EVENTS_AMOUNT = 3;

    private String endpoint;
    private Client client = ClientBuilder.newClient();
    private List<LoginAggregation> receive = new CopyOnWriteArrayList<>();
    private boolean completed;

    @Test
    public void testAlertMonitorEventStream() throws InterruptedException {
        givenAnApplicationEndpoint(getEndpoint() + "/monitor/stream");
        whenRequestSomeEvents(EVENTS_AMOUNT);
        thenVerifyAllEventsArrived();
    }

    protected abstract RestService getApp();

    private void givenAnApplicationEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private void whenRequestSomeEvents(int amount) throws InterruptedException {
        WebTarget target = client.target(endpoint);
        final CountDownLatch latch = new CountDownLatch(amount);

        SseEventSource source = SseEventSource.target(target).build();
        source.register(inboundSseEvent -> {
            receive.add(inboundSseEvent.readData(LoginAggregation.class, MediaType.APPLICATION_JSON_TYPE));
            latch.countDown();
        });

        source.open();
        completed = latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        source.close();
    }

    private void thenVerifyAllEventsArrived() {
        assertTrue(completed, "Not all expected kafka events has been consumed.");
    }

    private String getEndpoint() {
        return getApp().getHost() + ":" + getApp().getPort();
    }
}
