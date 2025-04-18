package io.quarkus.ts.websockets.next.endpoints;

import jakarta.inject.Inject;

import io.quarkus.websockets.next.OnBinaryMessage;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.vertx.core.buffer.Buffer;

@WebSocket(path = "/chat/{username}")
public class ChatWebSocket {

    @Inject
    WebSocketConnection connection;

    @OnOpen(broadcast = true)
    public String onOpen() {
        return connection.pathParam("username") + " joined";
    }

    @OnClose
    public void onClose() {
        String departure = connection.pathParam("username") + " left";
        connection.broadcast().sendTextAndAwait(departure);
    }

    @OnTextMessage(broadcast = true)
    public String onMessage(String message) {
        return connection.pathParam("username") + ": " + message;
    }

    @OnBinaryMessage(broadcast = true)
    public Buffer onBinaryMessage(Buffer message) {
        return message;
    }
}
