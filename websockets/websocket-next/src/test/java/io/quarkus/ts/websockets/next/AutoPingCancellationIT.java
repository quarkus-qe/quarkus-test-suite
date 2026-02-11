package io.quarkus.ts.websockets.next;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.websockets.next.client.WebSocketTestClient;

@Tag("https://github.com/quarkusio/quarkus/pull/47271")
@QuarkusScenario
public class AutoPingCancellationIT {
    private static final Logger LOG = Logger.getLogger(AutoPingCancellationIT.class);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.websockets-next.server.auto-ping-interval", "3s")
            .withProperty("quarkus.log.category.\"io.quarkus.websockets.next\".level", "DEBUG");

    @Test
    public void testAutoTimerCancellation() throws Exception {
        createAndCloseConnections();
        Thread.sleep(3000);
        verifyNoAutoPingErrors();
    }

    private void createAndCloseConnections() {
        List<WebSocketTestClient> clients = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(5);

        // Create 5 clients
        for (int i = 0; i < 5; i++) {
            final int clientId = i;
            new Thread(() -> {
                try {
                    WebSocketTestClient client = createClient("/chat/user" + clientId);
                    clients.add(client);
                    LOG.info("Client " + clientId + " connected");
                    client.send("Test from client " + clientId);
                    Thread.sleep(200 + (clientId * 50));
                    client.closeBlocking();
                    LOG.info("Client " + clientId + " closed");
                } catch (Exception e) {
                    LOG.error("Error with client " + clientId, e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        boolean allCompleted;
        try {
            allCompleted = latch.await(10, TimeUnit.SECONDS);
            if (!allCompleted) {
                LOG.warn("Timeout waiting for all clients to complete");
            } else {
                LOG.info("All clients completed successfully");
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted while waiting for clients to complete", e);
        }

        // Clean up any remaining open clients
        for (WebSocketTestClient client : clients) {
            try {
                if (client.isOpen()) {
                    client.close();
                }
            } catch (Exception e) {
                LOG.error("Failed to close client during cleanup: " + e.getMessage());
            }
        }
    }

    private void verifyNoAutoPingErrors() {
        String logs = app.getLogs().toString();

        boolean hasAutoPingErrors = logs.contains("Unable to send auto-ping") ||
                logs.contains("WebSocket is closed");

        assertFalse(hasAutoPingErrors,
                "No auto-ping errors should be present after WebSocket connections close");
    }

    private URI getUri(String path) throws URISyntaxException {
        return new URI(app.getURI(Protocol.WS).toString()).resolve(path);
    }

    private WebSocketTestClient createClient(String endpoint) throws URISyntaxException, InterruptedException {
        WebSocketTestClient client = new WebSocketTestClient(getUri(endpoint));
        boolean connected = client.connectBlocking();
        if (!connected) {
            LOG.error("Failed to connect WebSocket client to " + endpoint);
        }
        return client;
    }
}
