package io.quarkus.ts.websockets.next.endpoints;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnPongMessage;
import io.quarkus.websockets.next.WebSocket;
import io.vertx.core.buffer.Buffer;

@WebSocket(path = "/pingPong")
public class PingPongWebSocket {
    public static List<Long> pingsReceived = new ArrayList<>();
    public static List<Long> pongsReceived = new ArrayList<>();

    @OnPingMessage
    void ping(Buffer data) {
        pingsReceived.add(System.currentTimeMillis());
    }

    @OnPongMessage
    void pong(Buffer data) {
        pongsReceived.add(System.currentTimeMillis());
    }
}
