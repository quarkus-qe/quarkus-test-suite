package io.quarkus.ts.websockets.next;

import static io.restassured.RestAssured.given;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;

public abstract class BaseWebSocketIT {
    private static final Logger LOG = Logger.getLogger(BaseWebSocketIT.class);

    protected abstract RestService getServer();

    @Test
    public void basicTextTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/chat/alice", false);
        client.send("Hello world");

        assertMessage("alice joined", client);
        assertMessage("alice: Hello world", client);
    }

    @Test
    public void basicBinaryTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/chat/alice");

        byte[] data = HexFormat.of().parseHex("e04fd020ea3a6910a2d808002b30309d");
        client.send(data);

        assertArrayEquals(data, client.waitForAndGetBinaryMessage().array(),
                "Received binary data should be same as send");
    }

    @Test
    public void chatTest() throws URISyntaxException, InterruptedException {
        Client aliceClient = createClient("/chat/alice");
        Client bobClient = createClient("/chat/bob");
        Client charlieClient = createClient("/chat/charlie");

        bobClient.send("Hello there");
        assertMessage("bob: Hello there", aliceClient, bobClient, charlieClient);

        charlieClient.send("Hi bob");
        assertMessage("charlie: Hi bob", aliceClient, bobClient, charlieClient);
    }

    @Test
    public void reactiveTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/reactive");
        assertMessage("Hello", client);

        client.send("Lorem ipsum");
        assertMessage("Message: ", client);
        assertMessage("Lorem ipsum", client);
    }

    @Test
    public void onErrorEventTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/failing");
        getServer().logs().assertContains("Error on websocket: Websocket failed to open");

        client.send("Random failure");
        getServer().logs().assertContains("Error on websocket: Random failure");
    }

    @Test
    @DisabledOnNative(reason = "https://github.com/quarkusio/quarkus/issues/47269")
    public void nativeSerializationTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/serialization/native");
        assertMessage("{\"type\":\"OPEN\",\"message\":\"Connection opened\",\"payload\":null}", client);

        client.send("{\"type\":\"WRONG\",\"message\":\"oops\",\"payload\":null}");
        assertMessage(
                "{\"type\":\"MESSAGE\",\"message\":\"Wrong original message\",\"payload\":{\"Original message\":\"oops\"}}",
                client);
    }

    @Test
    public void customSerializationTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/serialization/custom");
        client.send("bob;hello");
        assertMessage("bob;received: hello", client);
    }

    @Test
    public void concurrencyTest() throws URISyntaxException, InterruptedException {
        /*
         * By default, quarkus WS processes messages in serial mode.
         * Meaning next message will be processed only after first one was finished.
         * In concurrent mode, messages can be processed in parallel.
         *
         * In this test message "block" will always block(wait) in the processing for 1.5 seconds.
         * In serial mode, it should always wait for it to finish before processing next one.
         * In concurrent mode, the later message is faster and should arrive first.
         */

        Client serialClient = createClient("/serial");
        Client concurentClient = createClient("/concurrent");

        serialClient.send("block");
        serialClient.send("go");
        assertMessage("block", serialClient);
        assertMessage("go", serialClient);

        concurentClient.send("block");
        concurentClient.send("go");
        assertMessage("go", concurentClient);
        assertMessage("block", concurentClient);
    }

    @Test
    public void userDataOnServerTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/userData");

        client.send("get");
        assertMessage("messages sent: 0", client);

        client.send("one");
        assertMessage("one", client);
        client.send("two");
        assertMessage("two", client);
        client.send("three");
        assertMessage("three", client);

        client.send("get");
        assertMessage("messages sent: 3", client);
    }

    @Test
    public void userDataInClientTest() throws URISyntaxException, InterruptedException {
        Client client = createClient("/chat/alice");
        // connect server-side client
        given().queryParam("username", "bob").get("/userDataRes/connect");

        try {
            // server-side client will respond to "login" message by sending its username (which is stored in user data)
            client.send("login");
            assertMessage("alice: login", client);
            // first username is set by the chat broker, second one by the server-side client
            assertMessage("bob: bob", client);
        } finally {
            given().get("/userDataRes/disconnect");
        }
    }

    @Test
    public void subWebSocketTest() throws URISyntaxException, InterruptedException {
        Client parentClient = createClient("/parent");
        Client nestedClient = createClient("/parent/nested");

        parentClient.send("foo");
        assertMessage("This is parent webSocket", parentClient);

        nestedClient.send("bar");
        assertMessage("This is nested webSocket", nestedClient);
    }

    // verify that custom logic for allowing/rejecting upgrade http-to-websocket works
    @Test
    public void httpToWebSocketUpgradeTest() throws URISyntaxException, InterruptedException {
        Client rejectClient = new Client(getUri("/parent"), true);
        rejectClient.addHeader("Reject", "");
        assertFalse(rejectClient.connectBlocking(), "Upgrade from http to websocket should be rejected");

        Client allowClient = new Client(getUri("/parent"), true);
        assertTrue(allowClient.connectBlocking(), "Upgrade from http to websocket should be allowed");
    }

    @Test
    public void pingPongTest() throws InterruptedException {
        // start websocket client on the server, WS client and server endpoints should start exchanging pings and pongs
        given().get("/pingPongRes/connect");

        // let them exchange ping pongs for a while
        Thread.sleep(5000);

        // close the client = stop the ping pong exchange
        given().get("/pingPongRes/disconnect");

        // assert that pings and pongs arrived in correct time intervals
        // default config is, that server should send ping every second, client every two seconds
        assertTimeDifference(getTimes("/pingPongRes/serverPings"), TimeUnit.SECONDS.toMillis(2));
        assertTimeDifference(getTimes("/pingPongRes/clientPongs"), TimeUnit.SECONDS.toMillis(2));

        assertTimeDifference(getTimes("/pingPongRes/clientPings"), TimeUnit.SECONDS.toMillis(1));
        assertTimeDifference(getTimes("/pingPongRes/serverPongs"), TimeUnit.SECONDS.toMillis(1));
    }

    @Test
    public void authenticatedChatTest() throws URISyntaxException, InterruptedException {
        // verify that unauthenticated client cannot join the authenticated chat
        Client anonymousClient = new Client(getUri("/authChat"));
        assertFalse(anonymousClient.connectBlocking(), "Anonymous connection should fail");

        // directly connect authenticated client and verify sent message
        Client client = createAuthenticatedClient("/authChat", "alice", "password");
        client.send("Hi");
        assertMessage("alice: Hi", client);

        // connect server-side client
        given()
                .queryParam("username", "bob")
                .queryParam("password", "secret")
                .get("/authChatRes/connect");
        try {
            // send message via server-side client
            given().queryParam("message", "Hello guys").get("/authChatRes/sendMessage");

            // verify that both direct and server-side client received the message
            assertMessage("bob: Hello guys", client);
            assertEquals("bob: Hello guys", given().get("/authChatRes/getLastMessage").asString(),
                    "Server side client should receive last message");
        } finally {
            given().get("authChatRes/disconnect");
        }
    }

    /**
     * Test endpoint secured only via config in properties file
     */
    @Test
    public void propertiesAuthenticationTest() throws URISyntaxException, InterruptedException {
        // verify that unauthenticated client cannot join the properties secured endpoint
        Client anonymousClient = new Client(getUri("/propertiesSecured"));
        assertFalse(anonymousClient.connectBlocking(), "Anonymous connection should fail");

        Client authenticatedClient = createAuthenticatedClient("/propertiesSecured", "alice", "password");
        authenticatedClient.send("hi");

        assertMessage("hi", authenticatedClient);
    }

    @Test
    public void authorizedChatTest() throws URISyntaxException, InterruptedException {
        // adminChat allows only admins to send messages, but anyone can listen
        // alice has the admin role, so can submit messages to chat
        Client adminClient = createAuthenticatedClient("/adminChat", "alice", "password");
        // bob is not admin, so cannot submit messages to chat
        Client userClient = createAuthenticatedClient("/adminChat", "bob", "secret");
        // even anonymous client can join and listen, but cannot send messages
        Client anonymousClient = createClient("/adminChat");

        adminClient.send("Howdy");
        assertMessage("alice: Howdy", adminClient, userClient, anonymousClient);

        userClient.send("oops");
        assertMessage("forbidden: bob", userClient);

        anonymousClient.send("trying to send");
        assertMessage("forbidden anonymous", anonymousClient);
    }

    @Test
    public void restrictedChatTest() throws URISyntaxException, InterruptedException {
        // adminOnlyChat allow only admins to join the chat
        // alice has the admin role, so can submit messages to chat
        Client adminClient = createAuthenticatedClient("/adminOnlyChat", "alice", "password");

        adminClient.send("Hello there");
        assertMessage("alice: Hello there", adminClient);

        Client userClient = new Client(getUri("/adminOnlyChat"));
        assertFalse(userClient.connectBlocking(), "User's connection to adminOnlyChat should fail");
    }

    private URI getUri(String with) throws URISyntaxException {
        return new URI(getServer().getURI(Protocol.WS).toString()).resolve(with);
    }

    protected void assertMessage(String expectedMessage, Client... clients) {
        for (Client client : clients) {
            Awaitility
                    .await()
                    .atMost(ofSeconds(2))
                    .untilAsserted(() -> assertEquals(expectedMessage, client.waitForAndGetMessage()));
        }
    }

    private List<Long> getTimes(String url) {
        return given().get(url).body().jsonPath().getList(".", Long.class);
    }

    /**
     * Used for ping & pong times check. Assert that two consecutive timestamps have difference of approximately the given time.
     */
    private void assertTimeDifference(List<Long> times, long expectedTimeDifference) {
        assertTrue(times.size() > 1, "There should be at least two times recorded");

        // add some tolerance to time difference
        long maxTimeDifference = Double.valueOf(expectedTimeDifference * 1.2).longValue();
        long minTimeDifference = Double.valueOf(expectedTimeDifference * 0.8).longValue();

        for (int i = 1; i < times.size(); i++) {
            long timeDifference = times.get(i) - times.get(i - 1);
            assertTrue(timeDifference < maxTimeDifference,
                    "Time difference should be less that: " + maxTimeDifference + " but was: " + timeDifference);
            assertTrue(timeDifference > minTimeDifference,
                    "Time difference should be more that: " + minTimeDifference + " but was: " + timeDifference);
        }
    }

    private Client createClient(String endpoint) throws URISyntaxException, InterruptedException {
        return createClient(endpoint, true);
    }

    private Client createAuthenticatedClient(String endpoint, String username, String password)
            throws URISyntaxException, InterruptedException {
        Client client = new Client(getUri(endpoint));
        String authString = username + ":" + password;
        client.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(authString.getBytes()));
        if (!client.connectBlocking()) {
            LOG.error("Websocket client fail to connect");
        }
        return client;
    }

    private Client createClient(String endpoint, boolean ignoreJoinMessages) throws URISyntaxException, InterruptedException {
        Client client = new Client(getUri(endpoint), ignoreJoinMessages);
        if (!client.connectBlocking()) {
            LOG.error("Websocket client fail to connect");
        }
        return client;
    }

    public static class Client extends WebSocketClient {
        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();
        private final LinkedBlockingDeque<ByteBuffer> binaryMessages = new LinkedBlockingDeque<>();
        private final boolean ignoreJoinMessages;

        public Client(URI serverUri, boolean ignoreJoinMessages) {
            super(serverUri);
            this.ignoreJoinMessages = ignoreJoinMessages;
        }

        public Client(URI serverUri) {
            super(serverUri);
            this.ignoreJoinMessages = true;
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            LOG.debug("New connection opened");
        }

        @Override
        public void onMessage(String message) {
            LOG.debug("Message received: " + message);
            if (!message.endsWith("joined") || !ignoreJoinMessages) {
                messages.add(message);
            }
        }

        // receive binary message
        @Override
        public void onMessage(ByteBuffer bytes) {
            LOG.debug("Binary message received: " + bytes.toString());
            binaryMessages.add(bytes);
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

        public ByteBuffer waitForAndGetBinaryMessage() throws InterruptedException {
            return binaryMessages.poll(2, TimeUnit.SECONDS);
        }
    }
}
