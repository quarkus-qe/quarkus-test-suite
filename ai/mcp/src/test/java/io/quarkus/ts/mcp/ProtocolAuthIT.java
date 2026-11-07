package io.quarkus.ts.mcp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.DiscoverPolicy;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.ProtocolClient;
import io.restassured.response.Response;

/**
 * Verifies, that authentication and authorization failures are visible, when the MCP client detects the protocol version.
 * Without a session the client authenticates every request, so the detection request ({@code server/discover}) is the
 * first one to fail.
 */
@QuarkusScenario
public class ProtocolAuthIT {
    // also the client id of the OIDC clients in protocol-auth-client.properties
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    // also the secret of the OIDC clients in protocol-auth-client.properties
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-oidc")
    }, classes = { FileServer.class, DiscoverPolicy.class }, properties = "server.properties")
    static final RestService server = new RestService()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.oidc.token.audience", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.profile", "oidc")
            .withProperty("%oidc.quarkus.http.auth.permission.mcp-endpoints.policy", DiscoverPolicy.NAME)
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
            .withProperty("quarkus.langchain4j.mcp.user.url", ProtocolAuthIT::serverUrl)
            .withProperty("quarkus.oidc-client.admin-oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc-client.user-oidc.auth-server-url", keycloak::getRealmUrl);

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

    protected KeycloakService keycloak() {
        return keycloak;
    }

    protected RestService server() {
        return server;
    }

    protected RestService client() {
        return client;
    }

    protected RestService unauthenticatedClient() {
        return unauthenticatedClient;
    }

    // Scenario 2, detection failure on authentication: no token
    @Test
    void missingToken() {
        assertAuthenticationFailure("anonymous");
    }

    // Scenario 2, detection failure on authentication: an invalid token
    @Test
    void invalidToken() {
        assertAuthenticationFailure("invalid");
    }

    // Scenario 2, detection failure on authentication: a valid token from the named OIDC client
    @Test
    void validToken() {
        // the detection request carries the token as well, otherwise the client would not end up modern
        client().given().get("/protocol/admin/modern").then().statusCode(200).body(is("true"));
        assertEquals("Hello, AI!", readFile("admin"));
    }

    // Scenario 2, detection failure on authentication: current behaviour for a token rejected only on server/discover
    @Test
    void detectionForbiddenFallsBack() {
        // the 403 carries an insufficient_scope challenge (see scopeChallengeIsNotTreatedAsLegacy), but the token is good
        // enough for the legacy initialization, so the client falls back to it
        client().given().get("/protocol/user/modern").then().statusCode(200).body(is("false"));
        assertEquals("Hello, AI!", readFile("user"));
    }

    // Scenario 2, detection failure on authentication: an insufficient_scope challenge on server/discover is not a legacy
    // server
    @Test
    @Disabled("Current behaviour: a detection request rejected with 403 and an insufficient_scope challenge silently falls"
            + " back to 2025-11-25, like any 4xx without a modern error body. Expected: the client treats the challenge as an"
            + " authorization problem, not as a legacy server, so it steps up or fails with the authorization cause. See"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/authorization#runtime-insufficient-scope-errors,"
            + " https://modelcontextprotocol.io/specification/2026-07-28/basic/authorization#step-up-authorization-flow")
    void scopeChallengeIsNotTreatedAsLegacy() {
        // quarkus-mcp-server-oidc adds the challenge to every 403, because resource-metadata.scopes is set in
        // server.properties
        String token = keycloak().createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT)
                .obtainAccessToken("test-normal-user", "test-normal-user").getToken();
        server().given().auth().oauth2(token).header("Mcp-Method", "server/discover").post("/mcp")
                .then().statusCode(403).header("WWW-Authenticate", containsString("error=\"insufficient_scope\""));

        // the user can not get the scope, so the client fails with the authorization cause
        client().given().get("/protocol/user/modern").then().statusCode(500).body(containsString("insufficient_scope"));
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
