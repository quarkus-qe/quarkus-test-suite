package io.quarkus.ts.websockets.next;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jboss.logging.Logger;

public class Client extends WebSocketClient {
    private static final Logger LOG = Logger.getLogger(Client.class);

    private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<ByteBuffer> binaryMessages = new LinkedBlockingDeque<>();
    private final boolean ignoreJoinMessages;

    public Client(URI serverUri, boolean ignoreJoinMessages) {
        super(serverUri);
        this.ignoreJoinMessages = ignoreJoinMessages;
    }

    public Client(URI serverUri) {
        super(serverUri);
        this.ignoreJoinMessages = true;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOG.debug("New connection opened");
    }

    @Override
    public void onMessage(String message) {
        LOG.debug("Message received: " + message);
        if (!message.endsWith("joined") || !ignoreJoinMessages) {
            messages.add(message);
        }
    }

    // receive binary message
    @Override
    public void onMessage(ByteBuffer bytes) {
        LOG.debug("Binary message received: " + bytes.toString());
        binaryMessages.add(bytes);
    }

    @Override
    public void onClose(int i, String reason, boolean b) {
        LOG.debug("WS connection closed for reason: " + reason);
    }

    @Override
    public void onError(Exception e) {
        LOG.error("Websocket Exception thrown: " + e.getMessage(), e);
    }

    public String waitForAndGetMessage() throws InterruptedException {
        return messages.poll(2, TimeUnit.SECONDS);
    }

    public ByteBuffer waitForAndGetBinaryMessage() throws InterruptedException {
        return binaryMessages.poll(2, TimeUnit.SECONDS);
    }
}
