package io.quarkus.ts.mcp;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Dependency;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.mcp.app.DiscoverPolicy;
import io.quarkus.ts.mcp.app.FileServer;
import io.quarkus.ts.mcp.app.ProtocolClient;

@OpenShiftScenario
public class OpenShiftProtocolAuthIT extends ProtocolAuthIT {

    @KeycloakContainer(runKeycloakInProdMode = true, image = "${rhbk.image}")
    static final KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

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
            .withProperty("_ignored", "resource_with_destination::/deployments|robot-readable.txt")
            .withProperty("working.folder", "/deployments");

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-oidc-client-mcp-auth-provider")
            }, classes = { ProtocolClient.class }, properties = "protocol-auth-client.properties")
    static final RestService client = new RestService()
            .withProperties(keycloak::getTlsProperties)
            .withProperty("quarkus.langchain4j.mcp.admin.url", () -> serverUrl(server))
            .withProperty("quarkus.langchain4j.mcp.user.url", () -> serverUrl(server))
            .withProperty("quarkus.oidc-client.admin-oidc.auth-server-url", keycloak::getRealmUrl)
            .withProperty("quarkus.oidc-client.user-oidc.auth-server-url", keycloak::getRealmUrl);

    @QuarkusApplication(boms = {
            @Dependency(artifactId = "quarkus-langchain4j-bom") }, dependencies = {
                    @Dependency(artifactId = "quarkus-rest"),
                    @Dependency(groupId = "io.quarkiverse.langchain4j", artifactId = "quarkus-langchain4j-mcp")
            }, classes = { ProtocolClient.class }, properties = "protocol-auth-unauthenticated-client.properties")
    static final RestService unauthenticatedClient = new RestService()
            .withProperty("quarkus.langchain4j.mcp.anonymous.url", () -> serverUrl(server))
            .withProperty("quarkus.langchain4j.mcp.invalid.url", () -> serverUrl(server));

    @Override
    protected KeycloakService keycloak() {
        return keycloak;
    }

    @Override
    protected RestService server() {
        return server;
    }

    @Override
    protected RestService client() {
        return client;
    }

    @Override
    protected RestService unauthenticatedClient() {
        return unauthenticatedClient;
    }
}
