package io.quarkus.ts.websockets.next;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketConnectOptions;

@QuarkusScenario
public class VertxWebSocketClientIT {
    private static final Logger LOGGER = Logger.getLogger(VertxWebSocketClientIT.class);
    private static final String ADMIN_USERNAME = "charlie";
    private static final String ADMIN_PASSWORD = "random";
    private static Vertx vertx;

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(ssl = true)
    static final RestService server = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    @BeforeAll
    static void setup() {
        vertx = Vertx.vertx();
    }

    @AfterAll
    static void cleanup() {
        if (vertx != null) {
            vertx.close();
        }
    }

    private String getBearerToken(String username, String password) {
        return keycloak.createAuthzClient("test-application-client", "test-application-client-secret")
                .obtainAccessToken(username, password).getToken();
    }

    private URI getEndpointUri(String path, Protocol protocol) {
        try {
            return new URI(server.getURI(protocol).toString()).resolve(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid uri " + e);
        }
    }

    @Test
    void successfulAuthorizationWithHeader() throws InterruptedException {
        String token = getBearerToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        URI wsUri = getEndpointUri("/bearer", Protocol.WS);

        WebSocketClient webSocketClient = vertx.createWebSocketClient();

        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(wsUri.getHost())
                .setPort(wsUri.getPort())
                .setURI(wsUri.getPath())
                .addHeader("Authorization", "Bearer " + token);
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<String> receivedMessage = new AtomicReference<>();
        List<String> allMessages = new ArrayList<>();

        webSocketClient.connect(options).onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
                WebSocket ws = asyncResult.result();
                LOGGER.info("WebSocket connection established successfully");
                connectionLatch.countDown();

                ws.textMessageHandler(message -> {
                    LOGGER.info("Message received: " + message);
                    receivedMessage.set(message);
                    allMessages.add(message);
                    messageLatch.countDown();
                    ws.close();
                });

                ws.writeTextMessage("Test message from Vertx client");
            } else {
                LOGGER.error("Error connecting: " + asyncResult.cause().getMessage());
                connectionLatch.countDown();
            }
        });

        assertTrue(connectionLatch.await(5, TimeUnit.SECONDS), "Should establish connection within timeout");
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Should receive response within timeout");

        String message = receivedMessage.get();
        assertNotNull(message, "Received message shouldn't be null");
        assertTrue(message.contains(ADMIN_USERNAME),
                "Message should contain the authenticated username: " + message);
    }

    @Test
    void failedAuthenticationWithoutHeader() throws InterruptedException {
        URI wsUri = getEndpointUri("/bearer", Protocol.WS);

        WebSocketClient client = vertx.createWebSocketClient();

        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(wsUri.getHost())
                .setPort(wsUri.getPort())
                .setURI(wsUri.getPath());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> connectionSuccess = new AtomicReference<>(false);

        client.connect(options).onComplete(ar -> {
            if (ar.succeeded()) {
                LOGGER.warn("Connection unexpectedly established without authorization");
                connectionSuccess.set(true);
            } else {
                LOGGER.info("Connection rejected as expected: " + ar.cause().getMessage());
                connectionSuccess.set(false);
            }
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(connectionSuccess.get(), "Connection shouldn't be established without authorization");
    }

}
