package io.quarkus.ts.websockets.next.resources;

import java.net.URI;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.ts.websockets.next.clients.UserDataClient;
import io.quarkus.websockets.next.WebSocketClientConnection;
import io.quarkus.websockets.next.WebSocketConnector;

@Path("/userDataRes")
public class UserDataResource {
    @Inject
    WebSocketConnector<UserDataClient> connector;
    WebSocketClientConnection connection = null;

    private final URI baseUri;

    public UserDataResource(@ConfigProperty(name = "quarkus.http.port") int httpPort) {
        this.baseUri = URI.create("http://localhost:" + httpPort);
    }

    @GET
    @Path("/connect")
    public void connectClient(@RestQuery String username) {
        connection = connector
                .baseUri(baseUri)
                .pathParam("username", username)
                .connectAndAwait();
    }

    @GET
    @Path("/disconnect")
    public void disconnect() {
        connection.closeAndAwait();
    }
}
