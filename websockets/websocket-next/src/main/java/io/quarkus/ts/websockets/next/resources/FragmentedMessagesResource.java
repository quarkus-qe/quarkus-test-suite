package io.quarkus.ts.websockets.next.resources;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.websockets.next.BasicWebSocketConnector;

@Path("/fragmentedMessages")
public class FragmentedMessagesResource {

    public static final int MESSAGE_COUNT = 10;

    @GET
    @Path("/basicClient")
    public List<String> basicClient(@RestQuery URI serverUri, @RestQuery boolean binary) throws InterruptedException {
        return receive(serverUri, binary ? "binary" : "text", binary);
    }

    @GET
    @Path("/ignoreBinaryMessages")
    public List<String> ignoreBinaryMessages(@RestQuery URI serverUri) throws InterruptedException {
        // The server sends binary messages followed by text messages. No binary handler is registered,
        // so the binary messages are discarded. All subsequent text messages must still be received.
        return receive(serverUri, "ignoredBinary", false);
    }

    private List<String> receive(URI serverUri, String type, boolean binary) throws InterruptedException {
        List<String> messages = new CopyOnWriteArrayList<>();
        CountDownLatch received = new CountDownLatch(MESSAGE_COUNT);
        BasicWebSocketConnector connector = BasicWebSocketConnector.create()
                .baseUri(serverUri)
                .path("/fragmentedMessages/" + type);
        if (binary) {
            connector.onBinaryMessage((c, message) -> {
                messages.add(message.toString());
                received.countDown();
            });
        } else {
            connector.onTextMessage((c, message) -> {
                messages.add(message);
                received.countDown();
            });
        }
        var connection = connector.connectAndAwait();
        try {
            if (!received.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Did not receive all fragmented messages");
            }
            return List.copyOf(messages);
        } finally {
            connection.closeAndAwait();
        }
    }
}
