package io.quarkus.ts.mcp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MinimalEndpoint;
import io.restassured.response.Response;

@QuarkusScenario
public class NamedOIDCClientIT {

    static final String NORMAL_USER = "test-normal-user";
    static final String ADMIN_USER = "test-admin-user";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak1 = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak2 = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-oidc")
    }, classes = { FileServer.class }, properties = "server.properties")
    static final RestService server1 = new RestService()
            .withProperties(keycloak1::getTlsProperties)
            .withProperty("quarkus.oidc.token.audience", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak1.getRealmUrl())
            .withProperty("quarkus.profile", "oidc")
            .withProperty("working.folder", () -> getFileFolder(NamedOIDCClientIT.class, "server1"));

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-oidc")
    }, classes = { FileServer.class }, properties = "server.properties")
    static final RestService server2 = new RestService()
            .withProperties(keycloak2::getTlsProperties)
            .withProperty("quarkus.oidc.token.audience", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak2.getRealmUrl())
            .withProperty("quarkus.profile", "oidc")
            .withProperty("%oidc.quarkus.http.auth.policy.admin-scope-required.roles-allowed", "test-admin-role,test-user-role")
            .withProperty("working.folder", () -> getFileFolder(NamedOIDCClientIT.class, "server2"));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-client-mcp-auth-provider")
            }, classes = { MCPClient.class, MinimalEndpoint.class }, properties = "client.properties")
    static final RestService adminClient = new ClientService(new RestService())
            .withClient("provider1")
            .withClient("provider2")
            .get()
            .withProperties(keycloak1::getTlsProperties) // keycloak2::getTlsProperties returns the same values
            .withProperty("quarkus.oidc-client.provider1.auth-server-url", keycloak1::getRealmUrl)
            .withProperty("quarkus.oidc-client.provider1.grant-options.password.username", ADMIN_USER)
            .withProperty("quarkus.oidc-client.provider1.grant-options.password.password", ADMIN_USER)
            .withProperty("quarkus.oidc-client.provider2.auth-server-url", keycloak2::getRealmUrl)
            .withProperty("quarkus.oidc-client.provider2.grant-options.password.username", ADMIN_USER)
            .withProperty("quarkus.oidc-client.provider2.grant-options.password.password", ADMIN_USER)
            .withProperty("quarkus.langchain4j.mcp.filesystem.oidc-client-name", "provider1")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server1.getURI(Protocol.HTTP).withPath("/mcp").toString())
            .withProperty("quarkus.langchain4j.mcp.minimal.oidc-client-name", "provider2")
            .withProperty("quarkus.langchain4j.mcp.minimal.transport-type", "streamable-http")
            .withProperty("quarkus.langchain4j.mcp.minimal.url",
                    () -> server2.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-client-mcp-auth-provider")
            }, classes = { MCPClient.class, MinimalEndpoint.class }, properties = "client.properties")
    static final RestService userClient = new ClientService(new RestService())
            .withClient("provider1")
            .withClient("provider2")
            .get()
            .withProperties(keycloak1::getTlsProperties) // keycloak2::getTlsProperties returns the same values
            .withProperty("quarkus.oidc-client.provider1.auth-server-url", keycloak1::getRealmUrl)
            .withProperty("quarkus.oidc-client.provider1.grant-options.password.username", NORMAL_USER)
            .withProperty("quarkus.oidc-client.provider1.grant-options.password.password", NORMAL_USER)
            .withProperty("quarkus.oidc-client.provider2.auth-server-url", keycloak2::getRealmUrl)
            .withProperty("quarkus.oidc-client.provider2.grant-options.password.username", NORMAL_USER)
            .withProperty("quarkus.oidc-client.provider2.grant-options.password.password", NORMAL_USER)
            .withProperty("quarkus.langchain4j.mcp.filesystem.oidc-client-name", "provider1")
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server1.getURI(Protocol.HTTP).withPath("/mcp").toString())
            .withProperty("quarkus.langchain4j.mcp.minimal.oidc-client-name", "provider2")
            .withProperty("quarkus.langchain4j.mcp.minimal.transport-type", "streamable-http")
            .withProperty("quarkus.langchain4j.mcp.minimal.url",
                    () -> server2.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @Test
    public void adminAllowed() {
        Response normal = adminClient.given().when().body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(200, normal.statusCode(), "Response is " + normal.body().asString());
        Assertions.assertEquals("Hello, AI!", normal.body().asString());

        Response minimal = adminClient.given().when().body("robot-readable.txt").post("/mcp-minimal/tools/readFile");
        Assertions.assertEquals(200, minimal.statusCode(), "Response is " + minimal.body().asString());
        Assertions.assertEquals("Hello, AI!", minimal.body().asString());
    }

    @Test
    public void userRejected() {
        Response response = userClient.given().body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(500, response.statusCode());
        userClient.logs().assertContains("Unexpected status code: 403");
    }

    @Test
    public void userAllowed() {
        //  minimal mcp client connects to MCP server2; this server allows access for normal user
        Response minimal = userClient.given().when().body("robot-readable.txt").post("/mcp-minimal/tools/readFile");
        Assertions.assertEquals(200, minimal.statusCode(), "Response is " + minimal.body().asString());
        Assertions.assertEquals("Hello, AI!", minimal.body().asString());
    }

    static class ClientService {
        private RestService wrapped;

        ClientService(RestService wrapped) {
            this.wrapped = wrapped;
        }

        public ClientService withClient(String clientName) {
            this.wrapped
                    // keycloak tls configuration is provided by getTlsProperties,
                    // but uses quarkus.oidc property, while we need oidc-client there
                    .withProperty("quarkus.oidc-client." + clientName + ".tls.tls-configuration-name", "keycloak")
                    .withProperty("quarkus.oidc-client." + clientName + ".client-id", CLIENT_ID_DEFAULT)
                    .withProperty("quarkus.oidc-client." + clientName + ".credentials.secret", CLIENT_SECRET_DEFAULT)
                    .withProperty("quarkus.oidc-client." + clientName + ".grant.type", "password");
            return this;

        }

        public RestService get() {
            return wrapped;
        }
    }

}
