package io.quarkus.ts.mcp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.restassured.response.Response;

/**
 * Verifies authentication, when the MCP client detects the protocol version. Without a session the client authenticates
 * every request, so the detection request ({@code server/discover}) is the first one to fail.
 */
@QuarkusScenario
public class ProtocolAuthIT {
    // also the client id of the OIDC client in protocol-auth-client.properties
    static final String CLIENT_ID_DEFAULT = "test-application-client";

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-oidc")
    }, classes = { FileServer.class }, properties = "server.properties")
    static final RestService server = new RestService()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.oidc.token.audience", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.profile", "oidc")
            .withProperty("working.folder", () -> getFileFolder(ProtocolAuthIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-client-mcp-auth-provider")
            }, classes = { ProtocolClient.class }, properties = "protocol-auth-client.properties")
    static final RestService client = new RestService()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.langchain4j.mcp.admin.url", ProtocolAuthIT::serverUrl)
            .withProperty("quarkus.oidc-client.admin-oidc.auth-server-url", keycloak::getRealmUrl);

    // without an auth provider, otherwise its default OIDC client would be used for MCP clients without a named one
    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp")
            }, classes = { ProtocolClient.class }, properties = "protocol-auth-unauthenticated-client.properties")
    static final RestService unauthenticatedClient = new RestService()
            .withProperty("quarkus.langchain4j.mcp.anonymous.url", ProtocolAuthIT::serverUrl)
            .withProperty("quarkus.langchain4j.mcp.invalid.url", ProtocolAuthIT::serverUrl);

    private static String serverUrl() {
        return serverUrl(server);
    }

    protected static String serverUrl(RestService service) {
        return service.getURI(Protocol.HTTP).withPath("/mcp").toString();
    }

    protected RestService client() {
        return client;
    }

    protected RestService unauthenticatedClient() {
        return unauthenticatedClient;
    }

    // Authentication during detection: no token
    @Test
    void missingToken() {
        assertAuthenticationFailure("anonymous");
    }

    // Authentication during detection: an invalid token
    @Test
    void invalidToken() {
        assertAuthenticationFailure("invalid");
    }

    // Authentication during detection: a valid token from the named OIDC client
    @Test
    void validToken() {
        // the detection request carries the token as well, otherwise the client would not end up modern
        client().given().get("/protocol/admin/modern").then().statusCode(200).body(is("true"));
        assertEquals("Hello, AI!", readFile("admin"));
    }

    private void assertAuthenticationFailure(String mcpClient) {
        Response response = unauthenticatedClient().given().pathParam("client", mcpClient).get("/protocol/{client}/modern");
        assertEquals(500, response.statusCode());
        assertTrue(response.asString().contains("Authentication required: HTTP 401"), response.asString());
    }

    private String readFile(String mcpClient) {
        Response response = client().given().contentType("text/plain; charset=UTF-8").body("""
                {"file":"robot-readable.txt"}
                """)
                .pathParam("client", mcpClient)
                .pathParam("tool", "filereader")
                .post("/protocol/{client}/tools/{tool}");
        assertEquals(200, response.statusCode(), response.asString());
        return response.asString();
    }
}
