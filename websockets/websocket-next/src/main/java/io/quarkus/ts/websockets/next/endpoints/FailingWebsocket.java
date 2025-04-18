package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/failing")
public class FailingWebsocket {
    @OnOpen()
    public void onOpen() {
        throw new RuntimeException("Websocket failed to open");
    }

    // used to verify that @onError endpoint is called
    @OnError
    public void onError(Exception failure) {
        Log.warn("Error on websocket: " + failure.getMessage());
    }

    @OnTextMessage
    public Multi<String> onMessage(String message) {
        return Multi.createFrom().failure(new RuntimeException(message));
    }
}
