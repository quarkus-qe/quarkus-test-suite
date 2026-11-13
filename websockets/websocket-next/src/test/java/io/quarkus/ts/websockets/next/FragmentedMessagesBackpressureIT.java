package io.quarkus.ts.websockets.next;

import static io.quarkus.ts.websockets.next.resources.FragmentedMessagesResource.MESSAGE_COUNT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.java_websocket.enums.Opcode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.websockets.next.client.WebSocketTestClient;

@Tag("QUARKUS-9724")
@QuarkusScenario
public class FragmentedMessagesBackpressureIT {

    private static final List<String> EXPECTED_MESSAGES = Collections.nCopies(MESSAGE_COUNT, "hello");

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.websockets-next.server.max-pending-messages", "1")
            .withProperty("quarkus.websockets-next.client.max-pending-messages", "1");

    @Test
    public void serverReceivesFragmentedTextMessages() throws Exception {
        verifyServer(Opcode.TEXT, false);
    }

    @Test
    public void serverReceivesFragmentedBinaryMessages() throws Exception {
        verifyServer(Opcode.BINARY, false);
    }

    @Test
    public void serverReceivesAlternatingCompleteAndFragmentedMessages() throws Exception {
        verifyServer(Opcode.TEXT, true);
    }

    @Test
    public void basicClientReceivesFragmentedTextMessages() {
        verifyClient(false, false);
    }

    @Test
    public void basicClientReceivesFragmentedBinaryMessages() {
        verifyClient(true, false);
    }

    @Test
    public void basicClientIgnoresUnconsumedBinaryMessages() {
        verifyClient(false, true);
    }

    private void verifyServer(Opcode opcode, boolean alternate) throws Exception {
        URI uri = URI.create(app.getURI(Protocol.WS).toString()).resolve("/serial");
        WebSocketTestClient client = new WebSocketTestClient(uri);
        try {
            assertTrue(client.connectBlocking(), "WebSocket connection should open");
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                if (alternate && i % 2 == 0) {
                    client.send(EXPECTED_MESSAGES.get(i));
                } else {
                    client.sendFragmentedFrame(opcode, UTF_8.encode("he"), false);
                    client.sendPing();
                    client.sendFragmentedFrame(opcode, UTF_8.encode("ll"), false);
                    client.sendFragmentedFrame(opcode, UTF_8.encode("o"), true);
                }
            }
            for (String expected : EXPECTED_MESSAGES) {
                if (opcode == Opcode.BINARY) {
                    var received = client.waitForAndGetBinaryMessage();
                    assertNotNull(received, "Server should echo every fragmented binary message");
                    assertArrayEquals(expected.getBytes(UTF_8), received.array());
                } else {
                    assertEquals(expected, client.waitForAndGetMessage());
                }
            }
        } finally {
            client.closeBlocking();
        }
    }

    private void verifyClient(boolean binary, boolean unconsumed) {
        List<String> received = app.given()
                .queryParam("binary", binary)
                .queryParam("unconsumed", unconsumed)
                .get("/fragmentedMessages/basicClient")
                .then().statusCode(200)
                .extract().jsonPath().getList("", String.class);
        assertEquals(EXPECTED_MESSAGES, received, "Client should receive every message");
    }
}
