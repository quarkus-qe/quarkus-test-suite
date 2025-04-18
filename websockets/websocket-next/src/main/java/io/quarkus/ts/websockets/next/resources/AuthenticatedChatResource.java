package io.quarkus.ts.websockets.next.resources;

import java.net.URI;
import java.util.Base64;
import java.util.LinkedList;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.websockets.next.BasicWebSocketConnector;
import io.quarkus.websockets.next.WebSocketClientConnection;

@Path("/authChatRes")
public class AuthenticatedChatResource {
    @Inject
    BasicWebSocketConnector connector;
    private WebSocketClientConnection connection = null;

    private final URI baseUri;

    private final LinkedList<String> messages = new LinkedList<>();

    public AuthenticatedChatResource(@ConfigProperty(name = "quarkus.http.port") int httpPort) {
        this.baseUri = URI.create("http://localhost:" + httpPort);
    }

    @Path("/connect")
    @GET
    public void connect(@RestQuery String username, @RestQuery String password) {
        messages.clear();

        String authString = username + ":" + password;
        connection = connector
                .baseUri(baseUri)
                .path("/authChat")
                .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(authString.getBytes()))
                .onTextMessage((c, message) -> messages.add(message))
                .connectAndAwait();
    }

    @GET
    @Path("/getLastMessage")
    public String getLastMessage() {
        return messages.getLast();
    }

    @GET
    @Path("/sendMessage")
    public void sendMessage(@RestQuery String message) {
        connection.sendTextAndAwait(message);
    }

    @GET
    @Path("/disconnect")
    public void disconnect() {
        connection.closeAndAwait();
    }
}
