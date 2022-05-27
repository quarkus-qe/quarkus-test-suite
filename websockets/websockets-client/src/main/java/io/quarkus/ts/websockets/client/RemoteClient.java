package io.quarkus.ts.websockets.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/user")
@ApplicationScoped
public class RemoteClient {
    @ConfigProperty(name = "app.chat.uri")
    String uri;

    @GET
    @Path("check/{username}")
    public String get(@PathParam("username") String name) throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/chat/" + name))) {
            return client.getMessage();
        }
    }

    @GET
    @Path("push")
    public List<String> multiple() throws Exception {
        Client client = new Client();
        try (Session session = connect(client, getUri("/push"))) {
            List<String> messages = new ArrayList<>();
            String current;
            while (null != (current = client.getMessage())) {
                messages.add(current);
            }
            return messages;
        }
    }

    private URI getUri(String with) throws URISyntaxException {
        return new URI(uri).resolve(with);
    }

    private static Session connect(Client client, URI uri) throws DeploymentException, IOException {
        return ContainerProvider.getWebSocketContainer().connectToServer(client, uri);
    }

    @ClientEndpoint
    public static class Client {
        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();

        @OnOpen
        public void open(Session session) {
            // Send a message to indicate that we are ready,
            // as the message handler may not be registered immediately after this callback.
            session.getAsyncRemote().sendText("_ready_");
        }

        @OnMessage
        void message(String msg) {
            messages.add(msg);
        }

        public String getMessage() throws InterruptedException {
            return messages.poll(1, TimeUnit.SECONDS);
        }
    }
}
