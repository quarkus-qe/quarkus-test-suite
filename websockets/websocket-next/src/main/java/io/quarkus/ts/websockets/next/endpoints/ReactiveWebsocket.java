package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@WebSocket(path = "/reactive")
public class ReactiveWebsocket {
    @OnOpen(broadcast = true)
    public Uni<String> onOpen() {
        return Uni.createFrom().item("Hello");
    }

    @OnTextMessage(broadcast = true)
    public Multi<String> onMessage(String message) {
        return Multi.createFrom().items("Message: ", message);
    }
}
