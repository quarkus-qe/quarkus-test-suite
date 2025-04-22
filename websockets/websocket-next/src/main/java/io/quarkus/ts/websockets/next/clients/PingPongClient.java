package io.quarkus.ts.websockets.next.clients;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.websockets.next.OnPingMessage;
import io.quarkus.websockets.next.OnPongMessage;
import io.quarkus.websockets.next.WebSocketClient;
import io.vertx.core.buffer.Buffer;

@WebSocketClient(path = "/pingPong")
public class PingPongClient {
    public static List<Long> pingsReceived = new ArrayList<>();
    public static List<Long> pongsReceived = new ArrayList<>();

    @OnPingMessage
    void ping(Buffer data) {
        pingsReceived.add(System.currentTimeMillis());
    }

    @OnPongMessage
    void pong(Buffer data) {
        pongsReceived.add(System.currentTimeMillis());
        if (pongsReceived.size() == 3) {
            throw new RuntimeException("You asked for an error, you've got it!");
        }
    }
}
