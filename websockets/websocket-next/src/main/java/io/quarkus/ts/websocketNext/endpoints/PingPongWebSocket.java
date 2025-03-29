package io.quarkus.ts.websocketNext.endpoints;

import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnPongMessage;
import io.quarkus.websockets.next.WebSocket;
import io.vertx.core.buffer.Buffer;

@WebSocket(path = "/pingPong")
public class PingPongWebSocket {
    public static int pingsReceived = 0;
    public static int pongsReceived = 0;

    @OnPingMessage
    void ping(Buffer data) {
        pingsReceived++;
    }

    @OnPongMessage
    void pong(Buffer data) {
        pongsReceived++;
    }
}
