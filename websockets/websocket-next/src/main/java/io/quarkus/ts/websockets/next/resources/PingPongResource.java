package io.quarkus.ts.websockets.next.resources;

import java.net.URI;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.ts.websockets.next.clients.PingPongClient;
import io.quarkus.ts.websockets.next.endpoints.PingPongWebSocket;
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
        PingPongClient.pongsReceived.clear();
        PingPongClient.pingsReceived.clear();

        PingPongWebSocket.pongsReceived.clear();
        PingPongWebSocket.pingsReceived.clear();

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
    public List<Long> getServerPings() {
        return PingPongWebSocket.pingsReceived;
    }

    @GET
    @Path("/serverPongs")
    public List<Long> getServerPongs() {
        return PingPongWebSocket.pongsReceived;
    }

    @GET
    @Path("/clientPings")
    public List<Long> getClientPings() {
        return PingPongClient.pingsReceived;
    }

    @GET
    @Path("/clientPongs")
    public List<Long> getClientPongs() {
        return PingPongClient.pongsReceived;
    }
}
