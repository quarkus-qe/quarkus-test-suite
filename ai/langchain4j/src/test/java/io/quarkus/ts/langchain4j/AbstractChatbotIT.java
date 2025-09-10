package io.quarkus.ts.langchain4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.URILike;

@DisabledOnNative(reason = "https://issues.redhat.com/browse/QUARKUS-6774")
public abstract class AbstractChatbotIT {
    protected static final Logger LOG = Logger.getLogger(AbstractChatbotIT.class);
    static final String DEFAULT_ARGS = "--no-transfer-progress -DskipTests=true -DskipITs=true -Dquarkus.platform.version=${QUARKUS_PLATFORM_VERSION} -Dquarkus.platform.group-id=${QUARKUS_PLATFORM_GROUP-ID}";
    private static final int TIMEOUT = 10;
    private List<String> answers;
    private WebSocket webSocket;

    static String getKey() {
        return ConfigProvider.getConfig().getValue("quarkus.langchain4j.openai.api-key", String.class);
    }

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        String socket = "/chatbot";
        URILike uri = getApp().getURI(Protocol.WS).withPath(socket);
        LOG.info("Connecting to: " + uri);
        answers = Collections.synchronizedList(new ArrayList<>());
        webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(uri.toString()), new ChatbotIT.WebSocketListener(answers)).get();
    }

    abstract RestService getApp();

    @AfterEach
    void tearDown() throws ExecutionException, InterruptedException {
        answers = null;
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").get();
    }

    @Test
    public void smoke() {
        Awaitility.await().atMost(TIMEOUT, TimeUnit.SECONDS).untilAsserted(() -> {
            Assertions.assertFalse(answers.isEmpty());
        });
        Assertions.assertEquals("Hello, I'm Bob, how can I help you?", answers.get(0));
    }

    @Test
    public void documentBasedAnswer() throws InterruptedException {
        // Input tokens are usually much cheaper, than output tokens, so let's stop the LLM from talking too much
        String prompt = "What is the opening deposit (in USD) for a standard savings account? Answer with number only";
        webSocket.sendText(prompt, true);

        // The answer is split to many messages due to Multi<String> in the Bot.
        // We need to wait for the end of the answer, detected as no new messages during a second.
        // The prompt should protect against this, but it is not guaranteed.
        int repeats = -1;
        int lastSize = 0;
        while (answers.size() <= 1 || answers.size() != lastSize) {
            if (++repeats > TIMEOUT) {
                LOG.warn("We have waited for: " + repeats + " seconds and it is too much!");
                break;
            }
            lastSize = answers.size();
            Thread.sleep(1000);
        }

        String response = String.join("", answers);
        Assertions.assertTrue(response.contains("25"));
    }

    class WebSocketListener implements WebSocket.Listener {
        private final List<String> answers;
        private StringBuilder current;

        WebSocketListener(List<String> answers) {
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
}
