package io.quarkus.ts.mcp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.mcp.app.Utils.getFileFolder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;

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
public class OIDCIT extends BasicMCPIT {

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
            .withProperty("working.folder", () -> getFileFolder(OIDCIT.class));

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(artifactId = "quarkus-oidc"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-mcp-auth-provider")
            }, classes = { MCPClient.class }, properties = "client.properties")
    static final RestService client = new RestService()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
            .withProperty("quarkus.langchain4j.mcp.filesystem.transport-type", "streamable-http")
            .withProperty("quarkus.langchain4j.mcp.filesystem.url",
                    () -> server.getURI(Protocol.HTTP).withPath("/mcp").toString());
    private AuthzClient authzClient;

    @BeforeEach
    public void setup() {
        authzClient = keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT);
    }

    @Test
    public void adminAllowed() {
        Response response = client()
                .body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(200, response.statusCode(), "Response is " + response.body().asString());
        Assertions.assertEquals("Hello, AI!", response.body().asString());
    }

    @Test
    public void userRejected() {
        String token = getAuthzClient().obtainAccessToken(NORMAL_USER, NORMAL_USER).getToken();
        Response response = app().given()
                .when().auth().oauth2(token)
                .body("robot-readable.txt").post("/mcp/tools/readFile");
        Assertions.assertEquals(500, response.statusCode());
        app().logs().assertContains("Unexpected status code: 403");
    }

    @Override
    public RequestSpecification client() {
        return client.given().when().auth().oauth2(getAuthzClient().obtainAccessToken(ADMIN_USER, ADMIN_USER).getToken());
    }

    AuthzClient getAuthzClient() {
        return authzClient;
    }

    @Override
    public RestService app() {
        return client;
    }

}
