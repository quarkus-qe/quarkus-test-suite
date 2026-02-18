package io.quarkus.ts.mcp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Session {
    private static final Logger LOG = Logger.getLogger(WebSocketIT.class);

    private final WebSocket webSocket;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> answers = Collections.synchronizedList(new ArrayList<>());

    int id = 0;

    Session(String url) {
        try {
            this.webSocket = HttpClient.newHttpClient().newWebSocketBuilder()
                    .buildAsync(URI.create(url), new WebSocketListener(answers)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to start WebSocket Client!", e);
        }
    }

    public static String initParamas() {
        return """
                 {
                  "protocolVersion" : "2025-11-25",
                  "clientInfo" : {
                    "name" : "custom",
                    "version" : "1.0"
                  },
                  "capabilities": {
                    "sampling": {
                      "tools": {}
                    },
                    "elicitation": {
                      "form": {},
                      "url": {}
                    }
                  }
                }

                """;
    }

    public void clearHistory() {
        answers.clear();
    }

    public void sendRequest(String method, String params) {
        this.clearHistory();
        String request = this.generateRequest(method, params);
        webSocket.sendText(request, true);
    }

    public void confirmReadiness() {
        this.sendRequest("notifications/initialized", null);
    }

    public void sendResponse(int requestId, String result) {
        this.clearHistory();
        String response = """
                {
                  "jsonrpc": "2.0",
                  "id": %d,
                  "result": %s
                }
                """.formatted(requestId, result);
        webSocket.sendText(response, true);
    }

    public List<JsonNode> getResponses() {
        waitForAnswers();
        return answers.stream().map(answer -> {
            try {
                return mapper.readTree(answer);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        })
                .toList();
    }

    public JsonNode getResponse() {
        waitForAnswers();
        String content = answers.get(answers.size() - 1);
        try {
            return mapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error, while parsing the following json: '%s'".formatted(content), e);
        }
    }

    public List<JsonNode> getToolsFromPaginatedAnswer(JsonNode current, String method) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode tool : current.get("tools")) {
            result.add(tool);
        }
        if (current.has("nextCursor")) {
            String cursor = current.get("nextCursor").asText();
            this.sendRequest(method, """
                     {
                      "cursor": "%s"
                     }
                    """.formatted(cursor));
            JsonNode next = this.getResponse().get("result");
            result.addAll(this.getToolsFromPaginatedAnswer(next, method));
        }
        return result;
    }

    private void waitForAnswers() {
        int repeats = -1;
        int lastSize = 0;
        while (answers.isEmpty() || answers.size() != lastSize) {
            if (++repeats > 10) {
                LOG.warn("We have waited for: " + repeats + " seconds and it is too much!");
                break;
            }
            lastSize = answers.size();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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
}
