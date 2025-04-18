package io.quarkus.ts.websockets.next.endpoints;

import jakarta.inject.Inject;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;

@WebSocket(path = "/userData")
public class UserDataWebSocket {
    private static final String MESSAGES_SENT = "MESSAGES_SENT";

    @Inject
    WebSocketConnection connection;

    @OnOpen()
    public void onOpen() {
        connection.userData().put(UserData.TypedKey.forInt(MESSAGES_SENT), 0);
    }

    @OnTextMessage(broadcast = true)
    public String onMessage(String message) {
        int messagesSent = connection.userData().get(UserData.TypedKey.forInt(MESSAGES_SENT));

        if (message.equals("get")) {
            return "messages sent: " + messagesSent;
        }
        messagesSent++;
        connection.userData().put(UserData.TypedKey.forInt(MESSAGES_SENT), messagesSent);
        return message;
    }
}
