package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.vertx.core.json.JsonObject;

@WebSocket(path = "/serialization/native")
public class NativeSerializationWebsocket {

    public enum MessageType {
        OPEN,
        MESSAGE,
        WRONG
    }

    public record WSMessage(MessageType type, String message, JsonObject payload) {

    }

    @OnOpen(broadcast = true)
    public WSMessage onOpen() {
        return new WSMessage(MessageType.OPEN, "Connection opened", null);
    }

    @OnTextMessage(broadcast = true)
    public WSMessage onMessage(WSMessage message) {
        if (message.type == MessageType.WRONG) {
            JsonObject jsonOriginMessage = new JsonObject();
            jsonOriginMessage.put("Original message", message.message);

            return new WSMessage(MessageType.MESSAGE, "Wrong original message", jsonOriginMessage);
        }
        return message;
    }
}
