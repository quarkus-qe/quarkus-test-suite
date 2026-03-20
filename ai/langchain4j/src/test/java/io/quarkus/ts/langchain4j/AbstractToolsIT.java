package io.quarkus.ts.langchain4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.URILike;
import io.quarkus.ts.langchain4j.auxiliary.WebSocketListener;

@QuarkusScenario
public abstract class AbstractToolsIT {
    private static final Logger LOG = Logger.getLogger(AbstractToolsIT.class);
    private static final String LONG_WORD = "Supercalifragilisticexpialidocious";

    protected abstract RestService getServer();

    protected abstract RestService getClient();

    @Test
    void serverCheck() {
        McpClientTransport transport = HttpClientStreamableHttpTransport
                .builder(getServer().getURI(Protocol.HTTP).toString())
                .endpoint("/mcp")
                .build();
        var client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .capabilities(McpSchema.ClientCapabilities.builder()
                        .build())
                .build();

        McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest.builder()
                .name("readFileContent")
                .arguments(Map.of("file", "playground/longword.txt"))
                .build());
        Assertions.assertEquals(1, result.content().size());
        McpSchema.Content actual = result.content().get(0);
        Assertions.assertEquals("text", actual.type());
        McpSchema.TextContent text = (McpSchema.TextContent) actual;
        Assertions.assertEquals(LONG_WORD, text.text());
    }

    @Test
    void askAI() throws ExecutionException, InterruptedException {
        List<String> answers = Collections.synchronizedList(new ArrayList<>());
        URILike uri = getClient().getURI(Protocol.WS).withPath("/chatbot");
        var webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(uri.toString()), new WebSocketListener(answers)).get();
        waitForAnswers(answers);
        Assertions.assertEquals("Hello, I am a filesystem robot, how can I help?", answers.get(0));
        answers.clear();
        String prompt = "Read the contents of the file longword.txt";
        webSocket.sendText(prompt, true);
        waitForAnswers(answers);
        Assertions.assertTrue(answers.get(0).contains(LONG_WORD),
                "AI was not able to read content of the file and returned this instead:" + answers);
    }

    private void waitForAnswers(List<String> answers) {
        int repeats = -1;
        int lastSize = 0;
        while (answers.isEmpty() || answers.size() != lastSize) {
            if (++repeats > 10) {
                LOG.warn("We have waited for: " + repeats / 2 + " seconds and it is too much!");
                break;
            }
            lastSize = answers.size();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
