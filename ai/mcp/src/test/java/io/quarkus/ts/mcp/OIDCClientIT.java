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
import io.quarkus.ts.mcp.app.MyResources;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class OIDCClientIT extends BasicMCPIT {

    static final String NORMAL_USER = "test-normal-user";
    static final String ADMIN_USER = "test-admin-user";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-oidc")
    }, classes = { FileServer.class, MyResources.class }, properties = "server.properties")
    static final RestService server = new RestService()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.oidc.token.audience", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.profile", "oidc")
            .withProperty("working.folder", () -> getFileFolder(OIDCClientIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-client-mcp-auth-provider")
            }, classes = { MCPClient.class }, properties = "client.properties")
    static final RestService adminClient = new ClientService()
            .withDefaultProperties()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.oidc-client.the-client.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc-client.the-client.grant-options.password.username", ADMIN_USER)
            .withProperty("quarkus.oidc-client.the-client.grant-options.password.password", ADMIN_USER)
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-client-mcp-auth-provider")
            }, classes = { MCPClient.class }, properties = "client.properties")
    static final RestService userClient = new ClientService()
            .withDefaultProperties()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.oidc-client.the-client.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc-client.the-client.grant-options.password.username", NORMAL_USER)
            .withProperty("quarkus.oidc-client.the-client.grant-options.password.password", NORMAL_USER)
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());

    @Test
    public void userRejected() {
        Response response = userClient.given()
                .body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(500, response.statusCode());
        userClient.logs().assertContains("Unexpected status code: 403");
    }

    @Test
    public void adminAllowed() {
        Response response = adminClient.given().when()
                .body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(200, response.statusCode(), "Response is " + response.body().asString());
        Assertions.assertEquals("Hello, AI!", response.body().asString());
    }

    @Override
    public RequestSpecification client() {
        return adminClient.given();
    }

    @Override
    public RestService app() {
        return adminClient;
    }

    static class ClientService extends RestService {
        public RestService withDefaultProperties() {
            return this
                    // keycloak tls configuration is provided by getTlsProperties,
                    // but uses quarkus.oidc property, while we need oidc-client there
                    .withProperty("quarkus.oidc-client.the-client.tls.tls-configuration-name", "keycloak")
                    .withProperty("quarkus.oidc-client.the-client.client-id", CLIENT_ID_DEFAULT)
                    .withProperty("quarkus.oidc-client.the-client.credentials.secret", CLIENT_SECRET_DEFAULT)
                    .withProperty("quarkus.oidc-client.the-client.grant.type", "password")
                    .withProperty("quarkus.langchain4j.mcp.filesystem.oidc-client-name", "the-client")
                    .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http");
        }
    }

}
