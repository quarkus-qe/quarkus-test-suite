package io.quarkus.ts.websockets.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.jboss.logging.Logger;

@ServerEndpoint("/push")
@ApplicationScoped
public class Pusher {
    private static final Logger LOG = Logger.getLogger(Chat.class);
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) throws ExecutionException, InterruptedException, TimeoutException {
        LOG.debug(message);
        RemoteEndpoint.Async remote = session.getAsyncRemote();
        remote.sendText("One").get(2, TimeUnit.SECONDS);
        remote.sendText("Two").get(2, TimeUnit.SECONDS);
        remote.sendText("Three").get(2, TimeUnit.SECONDS);
        remote.sendText("Four").get(2, TimeUnit.SECONDS);
    }
}
