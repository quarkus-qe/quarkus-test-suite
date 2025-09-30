package io.quarkus.qe.hibernate.hql;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;

// This class is copied from a Quarkus project with minor modifications
// https://github.com/quarkusio/quarkus/blob/main/extensions/devui/test-spi/src/main/java/io/quarkus/devui/tests/DevUIJsonRPCTest.java
public class DevUIJsonRpcClient {

    protected URI uri;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String namespace;
    private final String DOT = ".";
    private final Vertx vertx;
    private final HttpClient client;

    public DevUIJsonRpcClient(String namespace, String testUrl) {
        // The namespace changed to be compatible with MCP. We add some code here to be backward compatible
        if (namespace.contains(DOT)) {
            namespace = namespace.substring(namespace.lastIndexOf(DOT) + 1);
        }

        this.namespace = namespace;
        this.uri = URI.create(testUrl + "/q/dev-ui/json-rpc-ws");

        this.vertx = Vertx.vertx();
        this.client = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultHost(this.uri.getHost())
                .setDefaultPort(this.uri.getPort()));
    }

    public <T> T executeJsonRPCMethod(TypeReference typeReference, String methodName) throws Exception {
        return executeJsonRPCMethod(typeReference, methodName, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T executeJsonRPCMethod(TypeReference typeReference, String methodName, Map<String, Object> params)
            throws Exception {
        int id = sendRequest(methodName, params);
        return getJsonRPCResponse(typeReference, id);
    }

    public JsonNode executeJsonRPCMethod(String methodName) throws Exception {
        return executeJsonRPCMethod(methodName, null);
    }

    public JsonNode executeJsonRPCMethod(String methodName, Map<String, Object> params) throws Exception {
        return executeJsonRPCMethod(JsonNode.class, methodName, params);
    }

    public <T> T executeJsonRPCMethod(Class<T> classType, String methodName) throws Exception {
        return executeJsonRPCMethod(classType, methodName, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T executeJsonRPCMethod(Class<T> classType, String methodName, Map<String, Object> params) throws Exception {
        int id = sendRequest(methodName, params);
        return getJsonRPCResponse(classType, id);
    }

    public void close() {
        if (client != null) {
            client.close();
        }
        if (vertx != null) {
            vertx.close();
        }
    }

    protected JsonNode toJsonNode(String json) {
        try {
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(json);
            return mapper.readTree(parser);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T> T getJsonRPCResponse(TypeReference typeReference, int id) throws InterruptedException, IOException {
        return getJsonRPCResponse(typeReference, id, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T getJsonRPCResponse(TypeReference typeReference, int id, int loopCount)
            throws InterruptedException, IOException {
        JsonNode object = objectResultFromJsonRPC(id);
        if (object != null) {
            JavaType jt = mapper.getTypeFactory().constructType(typeReference);
            return (T) mapper.treeToValue(object, jt);
        }
        if (loopCount > 10)
            throw new RuntimeException("Too many recursions, message not returned for id [" + id + "]");
        return getJsonRPCResponse(typeReference, id, loopCount + 1);
    }

    private <T> T getJsonRPCResponse(Class<T> classType, int id) throws InterruptedException, IOException {
        return getJsonRPCResponse(classType, id, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T getJsonRPCResponse(Class<T> classType, int id, int loopCount) throws InterruptedException, IOException {
        JsonNode object = objectResultFromJsonRPC(id);
        if (object != null) {
            if (classType == null || classType.equals(JsonNode.class)) {
                return (T) object;
            } else if (classType.equals(String.class)) {
                return (T) object.asText();
            } else if (classType.equals(Boolean.class)) {
                return (T) Boolean.valueOf(object.asBoolean());
            } else if (classType.equals(Double.class)) {
                return (T) Double.valueOf(object.asDouble());
            } else if (classType.equals(Integer.class)) {
                return (T) Integer.valueOf(object.asInt());
            } else if (classType.equals(Long.class)) {
                return (T) Long.valueOf(object.asLong());
            } else {
                return mapper.treeToValue(object, classType);
            }
        }
        if (loopCount > 10)
            throw new RuntimeException("Too many recursions, message not returned for id [" + id + "]");
        return getJsonRPCResponse(classType, id, loopCount + 1);
    }

    private JsonNode objectResultFromJsonRPC(int id) throws InterruptedException, JsonProcessingException {
        return objectResultFromJsonRPC(id, 0);
    }

    private JsonNode objectResultFromJsonRPC(int id, int loopCount) throws InterruptedException, JsonProcessingException {
        if (RESPONSES.containsKey(id)) {
            WebSocketResponse response = RESPONSES.remove(id);
            if (response != null) {
                try {
                    ObjectNode json = (ObjectNode) new ObjectMapper().readTree(response.message());
                    JsonNode result = json.get("result");
                    if (result != null) {
                        return result.get("object");
                    }
                    return json;
                } catch (JsonProcessingException e) {
                    // Return plain string as a JSON node
                    ObjectNode fallback = mapper.createObjectNode();
                    fallback.put("message", response.message());
                    return fallback;
                }
            }
            return null;
        } else {
            if (loopCount > 10)
                throw new RuntimeException("Too many recursions, message not returned for id [" + id + "]");

            TimeUnit.SECONDS.sleep(3);
            return objectResultFromJsonRPC(id, loopCount + 1);
        }
    }

    private String createJsonRPCRequest(int id, String methodName, Map<String, Object> params) throws IOException {

        ObjectNode request = mapper.createObjectNode();

        request.put("jsonrpc", "2.0");
        request.put("id", id);
        request.put("method", this.namespace + "_" + methodName);
        ObjectNode jsonParams = mapper.createObjectNode();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> p : params.entrySet()) {
                JsonNode convertValue = mapper.convertValue(p.getValue(), JsonNode.class);
                jsonParams.putIfAbsent(p.getKey(), convertValue);
            }
        }
        request.set("params", jsonParams);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
    }

    private int sendRequest(String methodName, Map<String, Object> params) throws IOException {
        Random random = new Random();
        int id = random.nextInt(Integer.MAX_VALUE);
        String request = createJsonRPCRequest(id, methodName, params);

        WebSocketConnectOptions socketOptions = new WebSocketConnectOptions()
                .setHost(this.uri.getHost())
                .setPort(this.uri.getPort())
                .setURI(this.uri.getPath());

        client.webSocket(socketOptions, ar -> {
            if (ar.succeeded()) {
                WebSocket socket = ar.result();
                Buffer accumulatedBuffer = Buffer.buffer();

                socket.frameHandler((e) -> {
                    Buffer b = accumulatedBuffer.appendBuffer(e.binaryData());
                    if (e.isFinal()) {
                        RESPONSES.put(id, new WebSocketResponse(b.toString()));
                    }
                });

                socket.writeTextMessage(request);

                socket.exceptionHandler((e) -> {
                    RESPONSES.put(id, new WebSocketResponse(e));
                });
            } else {
                RESPONSES.put(id, new WebSocketResponse(ar.cause()));
            }
        });
        return id;
    }

    private final ConcurrentHashMap<Integer, WebSocketResponse> RESPONSES = new ConcurrentHashMap<>();

    private static class WebSocketResponse {
        private final String message;
        private final Throwable throwable;

        public WebSocketResponse(String message) {
            this.message = message;
            this.throwable = null;
        }

        public WebSocketResponse(Throwable throwable) {
            this.message = null;
            this.throwable = throwable;
        }

        String message() {
            if (throwable != null) {
                throw new IllegalStateException("Request failed: " + throwable.getMessage(), throwable);
            }
            return message;
        }
    }
}
