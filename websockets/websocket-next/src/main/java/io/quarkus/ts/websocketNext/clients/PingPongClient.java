package io.quarkus.ts.websocketNext.clients;

import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnPongMessage;
import io.quarkus.websockets.next.WebSocketClient;
import io.vertx.core.buffer.Buffer;

@WebSocketClient(path = "/pingPong")
public class PingPongClient {
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
