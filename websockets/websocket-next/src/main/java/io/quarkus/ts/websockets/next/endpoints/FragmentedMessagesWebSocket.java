package io.quarkus.ts.websockets.next.endpoints;

import jakarta.inject.Inject;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.vertx.core.buffer.Buffer;

@WebSocket(path = "/fragmentedMessages/{type}")
public class FragmentedMessagesWebSocket {

    private static final int MESSAGE_COUNT = 10;

    // Like quarkusio/quarkus#57079, send 100 KiB messages to exceed the configured 64 KiB frame size.
    public static final String MESSAGE = "x".repeat(100 * 1024);

    @Inject
    WebSocketConnection connection;

    @OnOpen
    public void onOpen() {
        switch (connection.pathParam("type")) {
            case "text" -> sendMessages(false);
            case "binary" -> sendMessages(true);
            case "ignoredBinary" -> {
                sendMessages(true);
                sendMessages(false);
            }
            default -> throw new IllegalArgumentException("Unsupported message type: " + connection.pathParam("type"));
        }
    }

    private void sendMessages(boolean binary) {
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            if (binary) {
                connection.sendBinaryAndAwait(Buffer.buffer(MESSAGE));
            } else {
                connection.sendTextAndAwait(MESSAGE);
            }
        }
    }
}
