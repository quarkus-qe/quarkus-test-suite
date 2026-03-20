package io.quarkus.ts.langchain4j.auxiliary;

import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.jboss.logging.Logger;

public class WebSocketListener implements WebSocket.Listener {
    private static final Logger LOG = Logger.getLogger(WebSocketListener.class);
    private final List<String> answers;
    private StringBuilder current;

    public WebSocketListener(List<String> answers) {
        this.answers = answers;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        LOG.info("Message: " + data);
        if (current == null) {
            current = new StringBuilder();
        }
        current.append(data);
        if (last) {
            answers.add(current.toString());
            current = null;
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }
}
