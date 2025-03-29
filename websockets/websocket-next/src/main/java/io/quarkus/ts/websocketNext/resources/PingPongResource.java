package io.quarkus.ts.websocketNext.resources;

import java.net.URI;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.ts.websocketNext.clients.PingPongClient;
import io.quarkus.ts.websocketNext.endpoints.PingPongWebSocket;
import io.quarkus.websockets.next.WebSocketClientConnection;
import io.quarkus.websockets.next.WebSocketConnector;

@Path("/pingPongRes")
public class PingPongResource {
    @Inject
    WebSocketConnector<PingPongClient> pingPongConnector;

    WebSocketClientConnection connection = null;

    private final URI baseUri;

    public PingPongResource(@ConfigProperty(name = "quarkus.http.port") int httpPort) {
        this.baseUri = URI.create("http://localhost:" + httpPort);
    }

    @GET
    @Path("/connect")
    public void connectClient() {
        //reset ping pong counters
        PingPongClient.pongsReceived = 0;
        PingPongClient.pingsReceived = 0;

        PingPongWebSocket.pongsReceived = 0;
        PingPongWebSocket.pingsReceived = 0;

        connection = pingPongConnector
                .baseUri(baseUri)
                .connectAndAwait();
    }

    @GET
    @Path("/disconnect")
    public void disconnectClient() {
        if (connection != null) {
            connection.closeAndAwait();
            connection = null;
        }
    }

    @GET
    @Path("/serverPings")
    public int getServerPings() {
        return PingPongWebSocket.pingsReceived;
    }

    @GET
    @Path("/serverPongs")
    public int getServerPongs() {
        return PingPongWebSocket.pongsReceived;
    }

    @GET
    @Path("/clientPings")
    public int getClientPings() {
        return PingPongClient.pingsReceived;
    }

    @GET
    @Path("/clientPongs")
    public int getClientPongs() {
        return PingPongClient.pongsReceived;
    }
}
