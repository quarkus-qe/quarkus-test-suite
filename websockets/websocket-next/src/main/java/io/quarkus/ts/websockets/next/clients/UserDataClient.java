package io.quarkus.ts.websockets.next.clients;

import jakarta.inject.Inject;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocketClient;
import io.quarkus.websockets.next.WebSocketClientConnection;

@WebSocketClient(path = "/chat/{username}")
public class UserDataClient {

    @Inject
    WebSocketClientConnection connection;

    @OnOpen
    void open() {
        connection.userData().put(UserData.TypedKey.forString("username"), connection.pathParam("username"));
    }

    @OnTextMessage
    String process(String message) {
        if (message.endsWith("login")) {
            return connection.userData().get(UserData.TypedKey.forString("username"));
        }
        return null;
    }
}
