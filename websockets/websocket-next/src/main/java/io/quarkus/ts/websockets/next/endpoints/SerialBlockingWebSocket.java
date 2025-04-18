package io.quarkus.ts.websockets.next.endpoints;

import io.quarkus.websockets.next.InboundProcessingMode;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "/serial", inboundProcessingMode = InboundProcessingMode.SERIAL)
public class SerialBlockingWebSocket {

    @OnTextMessage
    public String onMessage(String message) throws InterruptedException {
        if (message.equals("block")) {
            Thread.sleep(1500);
        }
        return message;
    }
}
