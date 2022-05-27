package io.quarkus.ts.websockets.producer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.jboss.logging.Logger;

@ServerEndpoint("/push")
@ApplicationScoped
public class Pusher {
    private static final Logger LOG = Logger.getLogger(Chat.class);

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        //todo messages are not sent on opening of session. Need to investigate and create reproducer
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
