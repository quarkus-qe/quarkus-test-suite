package io.quarkus.ts.mcp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.quarkus.ts.mcp.OIDCIT.ADMIN_USER;
import static io.quarkus.ts.mcp.OIDCIT.CLIENT_ID_DEFAULT;
import static io.quarkus.ts.mcp.OIDCIT.CLIENT_SECRET_DEFAULT;
import static io.quarkus.ts.mcp.OIDCIT.NORMAL_USER;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.MCPClient;
import io.quarkus.ts.mcp.app.MyResources;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@OpenShiftScenario
public class OpenShiftOIDCIT extends BasicMCPIT {

    @KeycloakContainer(runKeycloakInProdMode = true)
    static final KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication(boms = { @Dependency(artifactId = "quarkus-mcp-server-bom") }, dependencies = {
            @Dependency(artifactId = "quarkus-rest"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-http"),
            @Dependency(groupId = "io.quarkiverse.mcp", artifactId = "quarkus-mcp-server-oidc")
    }, classes = { FileServer.class, MyResources.class }, properties = "server.properties")
    static final RestService server = new RestService()
            .withProperty("quarkus.oidc.token.audience", CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.profile", "oidc")
            .withProperty("_ignored", "resource_with_destination::/deployments|robot-readable.txt")
            .withProperty("working.folder", "/deployments");

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(artifactId = "quarkus-oidc"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-mcp-auth-provider")
            }, classes = { MCPClient.class }, properties = "client.properties")
    static final RestService client = new RestService()
            .withProperty("quarkus.profile", "debug")
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperties(keycloak::getTlsProperties)
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
