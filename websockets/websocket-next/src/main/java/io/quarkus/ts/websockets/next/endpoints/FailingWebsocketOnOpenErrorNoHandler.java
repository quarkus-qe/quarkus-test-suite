package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/failing-onopen-error-no-handler")
public class FailingWebsocketOnOpenErrorNoHandler {
    @OnOpen
    public void onOpen() {
        throw new RuntimeException("Websocket failed to open");
    }

    @OnTextMessage
    public Multi<String> onMessage(String message) {
        return Multi.createFrom().failure(new RuntimeException(message));
    }
}
