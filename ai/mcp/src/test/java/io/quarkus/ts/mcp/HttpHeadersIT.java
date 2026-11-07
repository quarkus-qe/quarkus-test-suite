package io.quarkus.ts.mcp;

import static io.quarkus.test.services.Certificate.Format.PKCS12;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.McpServerMock.Mode;
import io.quarkus.ts.mcp.app.CustomHeadersSupplier;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.HeaderServer;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.restassured.response.Response;

/**
 * Verifies the HTTP headers, which the Quarkus streamable HTTP transport sends with the 2026-07-28 protocol: the protocol
 * headers, {@code Mcp-Param-*} headers mirroring tool arguments, custom headers and the TLS registry.
 * <p>
 * The exact headers are checked with a mock, which echoes them back; the real server checks, that they match the request
 * body.
 */
@QuarkusScenario
public class HttpHeadersIT {
    // the static header of the headers clients in http-headers-client.properties and http-headers-supplier-client.properties
    private static final String STATIC_HEADER = "X-Static";
    private static final String STATIC_VALUE = "static-value";

    static final McpServerMock mcpServerMock = new McpServerMock();

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http")
    }, classes = { FileServer.class,
            HeaderServer.class }, ssl = true, certificates = @Certificate(prefix = "mcp-server", format = PKCS12, configureKeystore = true, configureTruststore = true, tlsConfigName = "tls-server", configureHttpServer = true))
    static final RestService server = new RestService()
            .withProperty("quarkus.http.insecure-requests", "enabled")
            .withProperty("working.folder", () -> getFileFolder(HttpHeadersIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { ProtocolClient.class }, properties = "http-headers-client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.langchain4j.mcp.headers.url", () -> mcpServerMock.url(Mode.HEADERS))
            .withProperty("quarkus.langchain4j.mcp.real.url", () -> serverUrl(Protocol.HTTP))
            .withProperty("quarkus.langchain4j.mcp.unlisted.url", () -> serverUrl(Protocol.HTTP))
            .withProperty("quarkus.langchain4j.mcp.secure.url", () -> serverUrl(Protocol.HTTPS))
            .withProperty("quarkus.tls.mcp-tls.trust-store.p12.path", HttpHeadersIT::trustStore);

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
            }, classes = { ProtocolClient.class,
                    CustomHeadersSupplier.class }, properties = "http-headers-supplier-client.properties")
    static final RestService supplierClient = new RestService()
            .withProperty("quarkus.langchain4j.mcp.headers.url", () -> mcpServerMock.url(Mode.HEADERS));

    private static String serverUrl(Protocol protocol) {
        return server.getURI(protocol).withPath("/mcp").toString();
    }

    private static String trustStore() {
        return server.getProperty("quarkus.tls.tls-server.trust-store.p12.path").orElseThrow();
    }

    @BeforeAll
    static void startMcpServerMock() {
        mcpServerMock.start();
    }

    @AfterAll
    static void stopMcpServerMock() {
        mcpServerMock.close();
    }

    // Scenario 3, HTTP transport headers: protocol headers for tools/call
    @Test
    void toolCallProtocolHeaders() {
        client.given().get("/protocol/headers/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/headers/tools").then().statusCode(200);

        Map<String, String> headers = headers(executeTool("headers", "echo-headers", "{}"));
        assertEquals("2026-07-28", headers.get("mcp-protocol-version"));
        assertEquals("tools/call", headers.get("mcp-method"));
        assertEquals("echo-headers", headers.get("mcp-name"));
        assertFalse(headers.containsKey("mcp-session-id"), "A stateless request must not carry a session");
    }

    // Scenario 3, HTTP transport headers: protocol headers for resources/read
    @Test
    void resourceReadProtocolHeaders() {
        client.given().get("/protocol/headers/modern").then().statusCode(200).body(is("true"));

        Map<String, String> headers = headers(client.given().queryParam("uri", "file:///echo")
                .get("/protocol/headers/resource"));
        assertEquals("2026-07-28", headers.get("mcp-protocol-version"));
        assertEquals("resources/read", headers.get("mcp-method"));
        assertEquals("file:///echo", headers.get("mcp-name"));
    }

    // Scenario 3, HTTP transport headers: protocol headers for prompts/get
    @Test
    void promptGetProtocolHeaders() {
        client.given().get("/protocol/headers/modern").then().statusCode(200).body(is("true"));

        Map<String, String> headers = headers(client.given().get("/protocol/headers/prompts/echo"));
        assertEquals("2026-07-28", headers.get("mcp-protocol-version"));
        assertEquals("prompts/get", headers.get("mcp-method"));
        assertEquals("echo", headers.get("mcp-name"));
    }

    // Scenario 3, HTTP transport headers: Mcp-Param headers for non-ASCII, leading whitespace and nested parameters
    @Test
    void paramHeaders() {
        client.given().get("/protocol/headers/tools").then().statusCode(200).body(is("echo-headers"));
        Map<String, String> headers = headers(executeTool("headers", "echo-headers", """
                {"region":"Žilina","location":{"city":" Brno"},"count":3,"text":"not a header"}
                """));
        // non-ASCII values and values with leading whitespace are encoded
        assertEquals(encoded("Žilina"), headers.get("mcp-param-region"));
        assertEquals(encoded(" Brno"), headers.get("mcp-param-city"));
        assertEquals("3", headers.get("mcp-param-count"));
        assertEquals(3, headers.keySet().stream().filter(name -> name.startsWith("mcp-param-")).count(), headers.toString());
    }

    // Scenario 3, HTTP transport headers: tools with an invalid x-mcp-header are excluded without hiding valid ones
    @Test
    void invalidParamHeaderAnnotationsExcludeOnlyAffectedTools() {
        client.given().get("/protocol/headers/tools").then().statusCode(200).body(is("echo-headers"));
        client.logs().assertContains("Excluding tool 'invalid-token' from tools/list");
        client.logs().assertContains("Excluding tool 'invalid-duplicate' from tools/list");
        client.logs().assertContains("Excluding tool 'invalid-type' from tools/list");
    }

    // Scenario 3, HTTP transport headers: header.* configuration arrives alongside the protocol headers
    @Test
    void staticHeaders() {
        client.given().get("/protocol/headers/tools").then().statusCode(200);
        Map<String, String> headers = headers(executeTool("headers", "echo-headers", "{}"));
        assertEquals(STATIC_VALUE, headers.get(STATIC_HEADER.toLowerCase()));
        assertEquals("tools/call", headers.get("mcp-method"));
        assertEquals("echo-headers", headers.get("mcp-name"));
    }

    // Scenario 3, HTTP transport headers: the headers supplier arrives alongside the protocol headers
    @Test
    void suppliedHeaders() {
        supplierClient.given().get("/protocol/headers/tools").then().statusCode(200);
        Map<String, String> headers = headers(supplierClient.given().body("{}").post("/protocol/headers/tools/echo-headers"));
        assertEquals(CustomHeadersSupplier.VALUE, headers.get(CustomHeadersSupplier.HEADER.toLowerCase()));
        assertEquals("tools/call", headers.get("mcp-method"));
        assertEquals("echo-headers", headers.get("mcp-name"));
    }

    // Scenario 3, HTTP transport headers: the headers supplier does not replace header.* configuration
    @Test
    @Disabled("Current behaviour: a McpHeadersSupplier bean replaces the headers from 'header.*' configuration, so the"
            + " configured headers are not sent. Expected: the supplied headers are added to the configured ones. See"
            + " https://docs.quarkiverse.io/quarkus-langchain4j/dev/mcp.html#quarkus-langchain4j-mcp_quarkus-langchain4j-mcp-client-name-header-header-name")
    void suppliedHeadersKeepConfiguredHeaders() {
        supplierClient.given().get("/protocol/headers/tools").then().statusCode(200);
        Map<String, String> headers = headers(supplierClient.given().body("{}").post("/protocol/headers/tools/echo-headers"));
        assertEquals(CustomHeadersSupplier.VALUE, headers.get(CustomHeadersSupplier.HEADER.toLowerCase()));
        assertEquals(STATIC_VALUE, headers.get(STATIC_HEADER.toLowerCase()));
    }

    // Scenario 3, HTTP transport headers: the real server accepts the Mcp-Param headers
    @Test
    void paramHeadersAcceptedByServer() {
        client.given().get("/protocol/real/modern").then().statusCode(200).body(is("true"));
        client.given().get("/protocol/real/tools").then().statusCode(200).body(containsString("region"));
        // the server compares the decoded header with the argument
        Response response = executeTool("real", "region", """
                {"region":"Žilina ","value":"data"}
                """);
        assertEquals(200, response.statusCode(), response.asString());
        assertEquals("Žilina /data", response.asString());
    }

    // Scenario 3, HTTP transport headers: executeTool without a prior listTools omits headers with a warning
    @Test
    @Disabled("Current behaviour: without the tool list the client omits the Mcp-Param headers, the server rejects the call"
            + " with a HeaderMismatch error whose id is null, so the client can not match it to the call and waits for the"
            + " tool execution timeout. Expected, two separate fixes: the server (Quarkus MCP Server) returns the"
            + " HeaderMismatch error with the request id, as JSON-RPC 2.0 requires once the id could be read; and the"
            + " client (LangChain4j) calls tools/list and retries with the appropriate headers (SHOULD). See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/streamable-http#server-validation,"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/transports/streamable-http#client-behavior,"
            + " https://www.jsonrpc.org/specification#response_object")
    void toolCalledBeforeListingTools() {
        Response response = executeTool("unlisted", "region", """
                {"region":"north","value":"data"}
                """);
        client.logs().assertContains("Executing tool 'region' before the tool list is known, so no Mcp-Param headers can"
                + " be sent");
        assertEquals(200, response.statusCode(), response.asString());
        assertEquals("north/data", response.asString());
    }

    // Scenario 3, HTTP transport headers: TLS registry over HTTP
    @Test
    void tlsRegistry() {
        client.given().get("/protocol/secure/modern").then().statusCode(200).body(is("true"));
        assertEquals("Hello, AI!", readFile("secure"));
    }

    private static Response executeTool(String mcpClient, String tool, String arguments) {
        return client.given().contentType("text/plain; charset=UTF-8").body(arguments)
                .pathParam("client", mcpClient)
                .pathParam("tool", tool)
                .post("/protocol/{client}/tools/{tool}");
    }

    private static String readFile(String mcpClient) {
        Response response = executeTool(mcpClient, "filereader", """
                {"file":"robot-readable.txt"}
                """);
        assertEquals(200, response.statusCode(), response.asString());
        return response.asString();
    }

    private static Map<String, String> headers(Response response) {
        assertEquals(200, response.statusCode(), response.asString());
        return Arrays.stream(response.asString().split("\n"))
                .map(line -> line.split(": ", 2))
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair.length > 1 ? pair[1] : ""));
    }

    private static String encoded(String value) {
        return "=?base64?" + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8)) + "?=";
    }
}
