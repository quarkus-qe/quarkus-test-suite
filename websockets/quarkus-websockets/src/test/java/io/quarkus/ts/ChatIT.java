package io.quarkus.ts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class ChatIT {

    @QuarkusApplication
    static final RestService server = new RestService();

    @Test
    public void smoke() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/chat/stu"))) {
            session.getAsyncRemote().sendText("hello world");
            Assertions.assertEquals(">> stu: hello world", client.getMessage());
        }
    }

    @Test
    public void chatting() throws Exception {
        final var aliceChat = new Client();
        final var bobChat = new Client();

        try (Session alice = connect(aliceChat, getUri("/chat/alice"))) {
            try (Session bob = connect(bobChat, getUri("/chat/bob"))) {

                alice.getAsyncRemote().sendText("hello bob");
                Assertions.assertEquals(">> alice: hello bob", aliceChat.getMessage());
                Assertions.assertEquals(">> alice: hello bob", bobChat.getMessage());

                bob.getAsyncRemote().sendText("hello alice");
                Assertions.assertEquals(">> bob: hello alice", bobChat.getMessage());
                Assertions.assertEquals(">> bob: hello alice", aliceChat.getMessage());
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
                Assertions.assertEquals(">> athos: hello, friends", athosChat.getMessage());
                Assertions.assertEquals(">> athos: hello, friends", porthosChat.getMessage());
                Assertions.assertEquals(">> athos: hello, friends", aramisChat.getMessage());

                aramis.getAsyncRemote().sendText("Adios, Athos!");
                Assertions.assertEquals(">> aramis: Adios, Athos!", athosChat.getMessage());
                Assertions.assertEquals(">> aramis: Adios, Athos!", porthosChat.getMessage());
                Assertions.assertEquals(">> aramis: Adios, Athos!", aramisChat.getMessage());

                athos.close();
                Assertions.assertNull(athosChat.getMessage());
                Assertions.assertEquals("User athos left", porthosChat.getMessage());
                Assertions.assertEquals("User athos left", aramisChat.getMessage());
            }
        }
    }

    @Test
    public void i18n() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/chat/traveler"))) {
            session.getAsyncRemote().sendText("今日は přátelé, как дела? \uD83E\uDED6 ?");
            Assertions.assertEquals(">> traveler: 今日は přátelé, как дела? \uD83E\uDED6 ?", client.getMessage());
        }
    }

    @Test
    public void push() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/push"))) {
            Assertions.assertEquals("One", client.getMessage());
            Assertions.assertEquals("Two", client.getMessage());
            Assertions.assertEquals("Three", client.getMessage());
            Assertions.assertEquals("Four", client.getMessage());
        }
    }

    // TODO change following code when this will be fixed https://github.com/quarkus-qe/quarkus-test-framework/issues/263
    private URI getUri(String with) throws URISyntaxException {
        return new URI(server.getHost() + ":" + server.getPort()).resolve(with);
    }

    private Session connect(Client client, URI uri) throws DeploymentException, IOException {
        return ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
    }

    @ClientEndpoint
    public static class Client {
        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();
        private final Set<String> joins = new ConcurrentSkipListSet<>();

        @OnOpen
        public void open(Session session) {
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            session.getAsyncRemote().sendText("_ready_");
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
