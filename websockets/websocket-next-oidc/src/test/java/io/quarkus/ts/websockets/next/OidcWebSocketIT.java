package io.quarkus.ts.websockets.next;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class OidcWebSocketIT {
    private static final Logger LOG = Logger.getLogger(OidcWebSocketIT.class);

    private static final String ADMIN_USERNAME = "charlie";
    private static final String ADMIN_PASSWORD = "random";
    private static final String USER_USERNAME = "albert";
    private static final String USER_PASSWORD = "einstein";

    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static final RestService server = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    @Test
    public void tokenAuthenticatedTest() throws URISyntaxException, InterruptedException {
        Client unauthenticatedClient = new Client(getUri("/bearer"));
        assertFalse(unauthenticatedClient.connectBlocking(),
                "Unauthenticated user should not be able to connect to bearer-secured endpoint");

        String token = createToken(USER_USERNAME, USER_PASSWORD);
        Client albertClient = createTokenAuthenticatedClient("/bearer", token);

        albertClient.send("Hello relativity");
        assertMessage(USER_USERNAME + ": Hello relativity", albertClient);
    }

    @Test
    public void authorizedChatTest() throws URISyntaxException, InterruptedException {
        // adminChat allows only admins to send messages, but non-admins can listen
        Client adminClient = createTokenAuthenticatedClient("/adminChat", createToken(ADMIN_USERNAME, ADMIN_PASSWORD));
        Client userClient = createTokenAuthenticatedClient("/adminChat", createToken(USER_USERNAME, USER_PASSWORD));

        adminClient.send("Howdy");
        assertMessage(ADMIN_USERNAME + ": Howdy", adminClient, userClient);

        userClient.send("oops");
        assertMessage("forbidden: " + USER_USERNAME, userClient);
    }

    @Test
    public void serverSideClientTest() throws URISyntaxException, InterruptedException {
        Client userClient = createTokenAuthenticatedClient("/bearer", createToken(USER_USERNAME, USER_PASSWORD));

        String adminToken = createToken(ADMIN_USERNAME, ADMIN_PASSWORD);

        server.given().queryParam("token", adminToken).get("/authChatRes/connect");

        try {
            // send message via server-side client
            server.given().queryParam("message", "Hello guys").get("/authChatRes/sendMessage");

            // verify that both direct and server-side client received the message
            assertMessage(ADMIN_USERNAME + ": Hello guys", userClient);
            assertEquals(ADMIN_USERNAME + ": Hello guys", server.given().get("/authChatRes/getLastMessage").asString(),
                    "Server side client should receive last message");
        } finally {
            server.given().get("authChatRes/disconnect");
        }
    }

    private static URI getUri(String with) throws URISyntaxException {
        return new URI(server.getURI(Protocol.WS).toString()).resolve(with);
    }

    private void assertMessage(String expectedMessage, Client... clients) {
        for (Client client : clients) {
            Awaitility
                    .await()
                    .atMost(ofSeconds(2))
                    .untilAsserted(() -> assertEquals(expectedMessage, client.waitForAndGetMessage()));
        }
    }

    private static String createToken(String username, String password) {
        return keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT).obtainAccessToken(username, password)
                .getToken();
    }

    private Client createTokenAuthenticatedClient(String endpoint, String token)
            throws URISyntaxException, InterruptedException {
        Client client = new Client(getUri(endpoint));
        client.addHeader("Authorization", "Bearer " + token);
        if (!client.connectBlocking()) {
            LOG.error("Websocket client fail to connect");
        }
        return client;
    }

    public static class Client extends WebSocketClient {
        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();

        public Client(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            LOG.debug("New connection opened");
        }

        @Override
        public void onMessage(String message) {
            LOG.debug("Message received: " + message);
            messages.add(message);
        }

        @Override
        public void onClose(int i, String reason, boolean b) {
            LOG.debug("WS connection closed for reason: " + reason);
        }

        @Override
        public void onError(Exception e) {
            LOG.error("Websocket Exception thrown: " + e.getMessage(), e);
        }

        public String waitForAndGetMessage() throws InterruptedException {
            return messages.poll(2, TimeUnit.SECONDS);
        }
    }
}
