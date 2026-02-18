package io.quarkus.ts.mcp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.OuterIcon;

@QuarkusScenario
public class WebSocketIT extends BasicMCPIT {
    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-websocket")
    }, classes = { FileServer.class, OuterIcon.class })
    static final RestService server = new RestService()
            .withProperty("quarkus.mcp.server.traffic-logging.enabled", "true")
            .withProperty("quarkus.mcp.server.traffic-logging.text-limit", "1000")
            .withProperty("working.folder", () -> Path.of("target").toAbsolutePath().toString());

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { MCPClient.class })
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "websocket")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> getUrl(server));

    private static final Logger LOG = Logger.getLogger(WebSocketIT.class);

    private static String getUrl(RestService service) {
        return service.getURI(Protocol.WS).withPath("/mcp/ws").toString();
    }

    @Override
    public RestService client() {
        return client;
    }

    @Test
    void manualConnection() throws ExecutionException, InterruptedException, IOException {
        Session session = new Session(getUrl(server));
        String initParams = """
                 {
                  "protocolVersion" : "2025-11-25",
                  "capabilities" : {
                    "roots" : {
                      "listChanged" : true
                    }
                  },
                  "clientInfo" : {
                    "name" : "custom",
                    "version" : "1.0"
                  }
                }

                """;
        session.sendRequest("initialize", initParams);
        JsonNode initialization = session.getResponse();
        Assertions.assertEquals("mcp", initialization
                .get("result")
                .get("serverInfo")
                .get("name").asText());

        session.sendRequest("tools/list", null);
        JsonNode tools = session.getResponse();
        Assertions.assertEquals("image/svg", tools
                .get("result")
                .get("tools")
                .get(0)
                .get("icons")
                .get(0)
                .get("mimeType").asText());
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

    class Session {
        private final WebSocket webSocket;
        private final ObjectMapper mapper = new ObjectMapper();
        private final List<String> answers = Collections.synchronizedList(new ArrayList<>());;
        int id = 0;

        Session(String url) throws ExecutionException, InterruptedException {
            this.webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                    .buildAsync(URI.create(url), new WebSocketListener(answers)).get();
        }

        public void sendRequest(String method, String params) {
            answers.clear();
            String request = this.generateRequest(method, params);
            webSocket.sendText(request, true);
        }

        public JsonNode getResponse() throws InterruptedException, JsonProcessingException {
            int repeats = -1;
            int lastSize = 0;
            while (answers.isEmpty() || answers.size() != lastSize) {
                if (++repeats > 10) {
                    LOG.warn("We have waited for: " + repeats + " seconds and it is too much!");
                    break;
                }
                lastSize = answers.size();
                Thread.sleep(1000);
            }
            return mapper.readTree(String.join("", answers));
        }

        String generateRequest(String method, String parameters) {
            String params;
            if (parameters == null) {
                params = "";
            } else {
                params = ",\n\"params\":" + parameters;
            }
            return """
                    {
                      "jsonrpc": "2.0",
                      "id": %d,
                      "method": "%s"%s
                    }
                    """.formatted(id++, method, params);
        }
    }
}
