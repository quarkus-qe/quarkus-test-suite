package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

/**
 * This endpoint should be secured by config in application.properties.
 * Should only allow authenticated clients
 */
@WebSocket(path = "/propertiesSecured")
public class PropertiesSecuredWebSocket {

    @OnTextMessage(broadcast = true)
    public String onMessage(String message) {
        return message;
    }
}
