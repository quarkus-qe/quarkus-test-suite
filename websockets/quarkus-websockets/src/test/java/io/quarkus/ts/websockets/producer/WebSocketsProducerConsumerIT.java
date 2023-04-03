package io.quarkus.ts.websockets.producer;

import static java.time.Duration.ofSeconds;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

import org.awaitility.Awaitility;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class WebSocketsProducerConsumerIT {

    private static final Logger LOG = Logger.getLogger(WebSocketsProducerConsumerIT.class);

    @QuarkusApplication
    static final RestService server = new RestService();

    @Test
    public void smoke() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/chat/stu"))) {
            session.getAsyncRemote().sendText("hello world");
            assertMessage(">> stu: hello world", client);
        }
    }

    @Test
    public void chatting() throws Exception {
        final var aliceChat = new Client();
        final var bobChat = new Client();

        try (Session alice = connect(aliceChat, getUri("/chat/alice"))) {
            try (Session bob = connect(bobChat, getUri("/chat/bob"))) {

                broadcastPlainMsgAsync(alice, "hello bob");
                assertMessage(">> alice: hello bob", aliceChat, bobChat);

                broadcastPlainMsgAsync(bob, "hello alice");
                assertMessage(">> bob: hello alice", bobChat, aliceChat);
            }
        }
    }

    @Test
    public void chattingSync() throws Exception {
        final var aliceChat = new Client();
        final var bobChat = new Client();

        try (Session alice = connect(aliceChat, getUri("/chat/alice"))) {
            try (Session bob = connect(bobChat, getUri("/chat/bob"))) {

                broadcastPlainMsg(alice, "hello bob");
                assertMessage(">> alice: hello bob", aliceChat, bobChat);

                broadcastPlainMsg(bob, "hello alice");
                assertMessage(">> bob: hello alice", bobChat, aliceChat);
            }
        }
    }

    @Test
    public void trio() throws Exception {
        final var athosChat = new Client();
        final var porthosChat = new Client();
        final var aramisChat = new Client();
        Session athos = connect(athosChat, getUri("/chat/athos"));
        try (Session porthos = connect(porthosChat, getUri("/chat/porthos"))) {
            try (Session aramis = connect(aramisChat, getUri("/chat/aramis"))) {
                athos.getAsyncRemote().sendText("hello, friends");
                assertMessage(">> athos: hello, friends", athosChat, porthosChat, aramisChat);

                aramis.getAsyncRemote().sendText("Adios, Athos!");
                assertMessage(">> aramis: Adios, Athos!", athosChat, porthosChat, aramisChat);

                athos.close();
                Assertions.assertNull(athosChat.getMessage());
                assertMessage("User athos left", porthosChat, aramisChat);
            }
        }
    }

    @Test
    public void i18n() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/chat/traveler"))) {
            session.getAsyncRemote().sendText("今日は přátelé, как дела? \uD83E\uDED6 ?");
            assertMessage(">> traveler: 今日は přátelé, как дела? \uD83E\uDED6 ?", client);
        }
    }

    @Test
    public void push() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/push"))) {
            assertMessage("One", client);
            assertMessage("Two", client);
            assertMessage("Three", client);
            assertMessage("Four", client);
        }
    }

    // TODO change following code when this will be fixed https://github.com/quarkus-qe/quarkus-test-framework/issues/263
    private static URI getUri(String with) throws URISyntaxException {
        return new URI(server.getHost() + ":" + server.getPort()).resolve(with);
    }

    private static Session connect(Client client, URI uri) throws DeploymentException, IOException {
        return ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
    }

    private static void assertMessage(String expectedMessage, Client... clients) {
        for (Client client : clients) {
            // message has been sent asynchronously, therefore we should wait a little
            Awaitility
                    .await()
                    .atMost(ofSeconds(2))
                    .untilAsserted(() -> Assertions.assertEquals(expectedMessage, client.getMessage()));
        }
    }

    private void broadcastPlainMsgAsync(Session session, String msg) {
        session.getAsyncRemote().sendText(msg, result -> {
            if (result.getException() != null) {
                LOG.error("Unable to send message: " + msg);
                Assertions.fail(result.getException());
            }
        });
    }

    private void broadcastPlainMsg(Session session, String msg) {
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            LOG.error("Unable to send message: " + msg);
            Assertions.fail(e);
        }
    }

    @ClientEndpoint
    public static class Client {
        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();
        private final Set<String> joins = new ConcurrentSkipListSet<>();

        @OnOpen
        public void open(Session session) {
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            session.getAsyncRemote().sendText("_ready_", result -> {
                if (result.getException() != null) {
                    LOG.error("Connection not opened");
                    Assertions.fail(result.getException());
                }
            });
        }

        @OnMessage
        void message(String msg) {
            // TODO sometimes sessions are reopened several times without closing, need to check, if this is right
            if (!msg.endsWith("joined")) {
                messages.add(msg);
            }
        }

        public String getMessage() throws InterruptedException {
            return messages.poll(10, TimeUnit.SECONDS);
        }
    }
}
