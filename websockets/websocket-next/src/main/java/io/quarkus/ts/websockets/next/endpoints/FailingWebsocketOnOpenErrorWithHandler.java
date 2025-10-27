package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/failing-onopen-error-with-handler")
public class FailingWebsocketOnOpenErrorWithHandler {
    @OnOpen()
    public void onOpen() {
        throw new RuntimeException("Websocket failed to open");
    }

    // @OnError handles onOpen() failure, preventing onOpen error metric increment
    @OnError
    public void onError(Exception failure) {
        Log.warn("Error on websocket: " + failure.getMessage());
    }

    @OnTextMessage
    public Multi<String> onMessage(String message) {
        return Multi.createFrom().failure(new RuntimeException(message));
    }
}
