package io.quarkus.ts.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

/**
 * A minimal MCP server, which plays servers that the Quarkus MCP Server can not: a legacy-only one and misbehaving ones.
 * The stdio part depends only on the JDK, so it can run as a subprocess with just the test classes on the classpath. Inside
 * the test JVM it runs as an HTTP and a WebSocket server, where every {@link Mode} is served on its own path:
 * {@code /{mode}/mcp}.
 * <p>
 * The JSON handling is intentionally naive: requests are only inspected for their id and method, and the responses are
 * fixed.
 */
public class McpServerMock implements AutoCloseable {
    public static final String LEGACY_TOOL = "legacy-echo";
    public static final String LEGACY_TOOL_RESULT = "Hello from the legacy stub";

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(\\d+|\"[^\"]*\")");
    private static final Pattern METHOD = Pattern.compile("\"method\"\\s*:\\s*\"([^\"]+)\"");

    public enum Mode {
        /**
         * Speaks only {@code initialize}, {@code tools/list} and {@code tools/call}, answers anything else with
         * "method not found".
         */
        LEGACY,
        /**
         * Like {@link #LEGACY}, but never answers {@code server/discover}.
         */
        SILENT_DISCOVER,
        /**
         * Never answers {@code server/discover} nor {@code initialize}.
         */
        SILENT,
        /**
         * Like {@link #LEGACY}, but fails hard on {@code server/discover}: the stdio process exits, HTTP closes the
         * connection.
         */
        INTOLERANT,
        /**
         * Like {@link #LEGACY}, but answers {@code server/discover} with HTTP 500. Only for HTTP.
         */
        SERVER_ERROR,
        /**
         * A 2026-07-28 server, which echoes request headers of {@code tools/call}, {@code resources/read} and
         * {@code prompts/get} and declares invalid {@code x-mcp-header} annotations on some tools. Only for HTTP.
         */
        HEADERS;

        public String path() {
            return "/" + name().toLowerCase().replace('_', '-') + "/mcp";
        }
    }

    private final HttpServer httpServer;
    private final WebSocketServer webSocketServer;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final CountDownLatch stopped = new CountDownLatch(1);

    /**
     * Creates the mock as an HTTP and a WebSocket server in the current JVM. Both bind a random port right away, so
     * {@link #url(Mode)} and {@link #webSocketUrl(Mode)} are known before {@link #start()}.
     */
    public McpServerMock() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind the MCP server mock", e);
        }
        for (Mode mode : Mode.values()) {
            httpServer.createContext(mode.path(), exchange -> handle(mode, exchange));
        }
        httpServer.setExecutor(executor);
        webSocketServer = new WebSocketServer();
    }

    public void start() {
        httpServer.start();
    }

    public String url(Mode mode) {
        return "http://localhost:" + httpServer.getAddress().getPort() + mode.path();
    }

    /**
     * Only {@link Mode#LEGACY}, {@link Mode#SILENT_DISCOVER}, {@link Mode#SILENT} and {@link Mode#INTOLERANT}, which closes
     * the WebSocket on {@code server/discover}.
     */
    public String webSocketUrl(Mode mode) {
        return "ws://localhost:" + webSocketServer.port() + mode.path();
    }

    /**
     * @return the value of the {@code command} property of a stdio MCP client, which runs the mock in the given mode
     */
    public static String stdioCommand(Mode mode) {
        String classes = Path.of("target", "test-classes").toAbsolutePath().toString().replaceAll("\\\\", "/");
        return String.join(",", "java", "-cp", classes, McpServerMock.class.getName(), mode.name());
    }

    @Override
    public void close() {
        stopped.countDown();
        httpServer.stop(0);
        executor.shutdownNow();
        webSocketServer.close();
    }

    public static void main(String[] args) throws IOException {
        Mode mode = Mode.valueOf(args[0]);
        // the protocol owns stdout, so make sure nothing else ends up there
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        System.setOut(System.err);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            String method = find(METHOD, line);
            if ("server/discover".equals(method)) {
                if (mode == Mode.INTOLERANT) {
                    System.err.println("Unknown method server/discover, exiting");
                    System.exit(1);
                }
                if (mode == Mode.SILENT_DISCOVER || mode == Mode.SILENT) {
                    continue;
                }
            }
            if ("initialize".equals(method) && mode == Mode.SILENT) {
                continue;
            }
            String response = legacyResponse(find(ID, line), method);
            if (response != null) {
                out.println(response);
            }
        }
    }

    private void handle(Mode mode, HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String id = find(ID, body);
            String method = find(METHOD, body);
            if ("server/discover".equals(method)) {
                switch (mode) {
                    case SILENT_DISCOVER, SILENT -> {
                        awaitStop();
                        return;
                    }
                    case INTOLERANT -> {
                        // no response at all, just drop the connection
                        exchange.getResponseBody().close();
                        return;
                    }
                    case SERVER_ERROR -> {
                        send(exchange, 500, "{\"error\":\"Unknown method\"}");
                        return;
                    }
                    default -> {
                    }
                }
            }
            if ("initialize".equals(method) && mode == Mode.SILENT) {
                awaitStop();
                return;
            }
            String response = mode == Mode.HEADERS
                    ? modernResponse(id, method, exchange.getRequestHeaders())
                    : legacyResponse(id, method);
            if (response == null) {
                exchange.sendResponseHeaders(202, -1);
            } else {
                send(exchange, 200, response);
            }
        }
    }

    private void awaitStop() {
        try {
            stopped.await(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String legacyResponse(String id, String method) {
        if (id == null) {
            // notifications do not get any response
            return null;
        }
        return switch (method) {
            case "initialize" -> result(id,
                    """
                            {"protocolVersion":"2025-11-25","capabilities":{"tools":{}},"serverInfo":{"name":"legacy-stub","version":"1.0"}}""");
            case "tools/list" -> result(id, """
                    {"tools":[{"name":"%s","description":"Says hello","inputSchema":{"type":"object","properties":{}}}]}"""
                    .formatted(LEGACY_TOOL));
            case "tools/call" -> result(id, """
                    {"content":[{"type":"text","text":"%s"}],"isError":false}""".formatted(LEGACY_TOOL_RESULT));
            case "ping" -> result(id, "{}");
            default -> methodNotFound(id, method);
        };
    }

    private static String modernResponse(String id, String method, Map<String, List<String>> headers) {
        if (id == null) {
            return null;
        }
        return switch (method) {
            case "server/discover" -> result(id, """
                    {"supportedVersions":["2026-07-28"],"capabilities":{"tools":{},"resources":{},"prompts":{}},\
                    "_meta":{"io.modelcontextprotocol/serverInfo":{"name":"headers-stub","version":"1.0"}}}""");
            case "tools/list" -> result(id, """
                    {"tools":[
                      {"name":"echo-headers","inputSchema":{"type":"object","properties":{
                        "region":{"type":"string","x-mcp-header":"Region"},
                        "location":{"type":"object","properties":{"city":{"type":"string","x-mcp-header":"City"}}},
                        "count":{"type":"integer","x-mcp-header":"Count"},
                        "text":{"type":"string"}}}},
                      {"name":"invalid-token","inputSchema":{"type":"object","properties":{
                        "region":{"type":"string","x-mcp-header":"Not a token"}}}},
                      {"name":"invalid-duplicate","inputSchema":{"type":"object","properties":{
                        "first":{"type":"string","x-mcp-header":"Same"},
                        "second":{"type":"string","x-mcp-header":"same"}}}},
                      {"name":"invalid-type","inputSchema":{"type":"object","properties":{
                        "amount":{"type":"number","x-mcp-header":"Amount"}}}}
                    ]}""");
            case "tools/call" -> result(id, """
                    {"content":[{"type":"text","text":"%s"}],"isError":false}""".formatted(echo(headers)));
            case "resources/read" -> result(id, """
                    {"contents":[{"uri":"file:///echo","text":"%s"}]}""".formatted(echo(headers)));
            case "prompts/get" -> result(id, """
                    {"messages":[{"role":"user","content":{"type":"text","text":"%s"}}]}""".formatted(echo(headers)));
            default -> methodNotFound(id, method);
        };
    }

    /**
     * @return request headers, one "name: value" per line, names in lower case and sorted
     */
    private static String echo(Map<String, List<String>> headers) {
        Map<String, String> sorted = new TreeMap<>();
        headers.forEach((name, values) -> sorted.put(name.toLowerCase(), String.join(",", values)));
        String text = sorted.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String result(String id, String result) {
        return "{\"jsonrpc\":\"2.0\",\"id\":%s,\"result\":%s}".formatted(id, result.replace("\n", ""));
    }

    private static String methodNotFound(String id, String method) {
        return "{\"jsonrpc\":\"2.0\",\"id\":%s,\"error\":{\"code\":-32601,\"message\":\"Method not found: %s\"}}"
                .formatted(id, method);
    }

    private static String find(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Kept in its own class, so the stdio subprocess, which has only the test classes on its classpath, never loads Vert.x.
     * Unlike the HTTP server it serves right away, but nothing connects to it before {@link #start()} anyway.
     */
    private static final class WebSocketServer {
        private final Vertx vertx = Vertx.vertx();
        private final io.vertx.core.http.HttpServer server;

        WebSocketServer() {
            server = vertx.createHttpServer()
                    .webSocketHandler(WebSocketServer::handle)
                    .listen(0, "localhost")
                    .toCompletionStage().toCompletableFuture().join();
        }

        int port() {
            return server.actualPort();
        }

        void close() {
            vertx.close().toCompletionStage().toCompletableFuture().join();
        }

        private static void handle(ServerWebSocket webSocket) {
            Mode mode = null;
            for (Mode candidate : Mode.values()) {
                if (candidate.path().equals(webSocket.path())) {
                    mode = candidate;
                }
            }
            if (mode == null) {
                webSocket.reject(404);
                return;
            }
            Mode served = mode;
            webSocket.textMessageHandler(message -> {
                String method = find(METHOD, message);
                if ("server/discover".equals(method)) {
                    if (served == Mode.INTOLERANT) {
                        webSocket.close();
                        return;
                    }
                    if (served == Mode.SILENT_DISCOVER || served == Mode.SILENT) {
                        return;
                    }
                }
                if ("initialize".equals(method) && served == Mode.SILENT) {
                    return;
                }
                String response = legacyResponse(find(ID, message), method);
                if (response != null) {
                    webSocket.writeTextMessage(response);
                }
            });
        }
    }
}
