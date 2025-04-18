package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "/parent")
public class ParentWebSocket {

    @OnTextMessage
    public String onMessage(String message) {
        return "This is parent webSocket";
    }

    @WebSocket(path = "/nested")
    public static class NestedWebSocket {
        @OnTextMessage
        public String onMessage(String message) {
            return "This is nested webSocket";
        }
    }
}
