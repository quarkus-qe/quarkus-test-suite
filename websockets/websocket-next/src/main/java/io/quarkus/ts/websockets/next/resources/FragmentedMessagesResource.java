package io.quarkus.ts.websockets.next.resources;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.websockets.next.BasicWebSocketConnector;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.http.WebSocketFrame;

@Path("/fragmentedMessages")
public class FragmentedMessagesResource {

    public static final int MESSAGE_COUNT = 10;

    @Inject
    Vertx vertx;

    @GET
    @Path("/basicClient")
    public List<String> basicClient(@RestQuery boolean binary, @RestQuery boolean unconsumed) throws Exception {
        var server = vertx.createHttpServer().webSocketHandler(ws -> {
            if (unconsumed) {
                sendFragments(ws, true);
            }
            sendFragments(ws, binary);
        }).listen(0).toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        try {
            List<String> messages = new CopyOnWriteArrayList<>();
            CountDownLatch received = new CountDownLatch(MESSAGE_COUNT);
            CountDownLatch pings = new CountDownLatch(MESSAGE_COUNT);
            BasicWebSocketConnector connector = BasicWebSocketConnector.create()
                    .baseUri("ws://localhost:" + server.actualPort())
                    .onPing((c, message) -> pings.countDown());
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
                if (!pings.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Did not receive all ping frames");
                }
                return List.copyOf(messages);
            } finally {
                connection.closeAndAwait();
            }
        } finally {
            server.close().toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
        }
    }

    private static void sendFragments(WebSocketBase ws, boolean binary) {
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            ws.writeFrame(binary ? WebSocketFrame.binaryFrame(Buffer.buffer("he"), false)
                    : WebSocketFrame.textFrame("he", false));
            ws.writePing(Buffer.buffer("ping"));
            ws.writeFrame(WebSocketFrame.continuationFrame(Buffer.buffer("ll"), false));
            ws.writeFrame(WebSocketFrame.continuationFrame(Buffer.buffer("o"), true));
        }
    }
}
