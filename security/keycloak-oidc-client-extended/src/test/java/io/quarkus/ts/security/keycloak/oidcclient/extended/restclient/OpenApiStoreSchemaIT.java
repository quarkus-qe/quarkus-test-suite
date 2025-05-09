package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.smallrye.openapi.runtime.io.Format;
import io.vertx.core.json.JsonObject;

@QuarkusScenario
public class OpenApiStoreSchemaIT {

    private static final Logger LOGGER = Logger.getLogger(OpenApiStoreSchemaIT.class.getName());
    private static final String directory = "target/generated/jakarta-rest/";
    private static final String OPEN_API_DOT = "openapi.";

    private static final String YAML = Format.YAML.toString().toLowerCase();
    private static final String YAML_FILE_NAME = OPEN_API_DOT + YAML;
    private static final Path YAML_FILE_NAME_FULL_PATH = Paths.get(directory + YAML_FILE_NAME);

    private static final String JSON = Format.JSON.toString().toLowerCase();
    private static final String JSON_FILE_NAME = OPEN_API_DOT + JSON;
    private static final Path JSON_FILE_NAME_FULL_PATH = Paths.get(directory + JSON_FILE_NAME);

    private static final String EXPECTED_TAGS = "[{\"name\":\"Ping\",\"description\":\"Ping API\"},{\"name\":\"Pong\",\"description\":\"Pong API\"}]";
    private static final String EXPECTED_INFO = "{\"title\":\"security-keycloak-oidc-client-extended API\",\"version\":\"1.0.0-SNAPSHOT\"}";

    @KeycloakContainer(command = { "start-dev", "--import-realm", "--features=token-exchange" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", keycloak::getRealmUrl);

    // QUARKUS-716
    @Test
    public void testJsonOpenApiPathAccessResource() throws IOException {
        assertTrue(Files.exists(JSON_FILE_NAME_FULL_PATH), JSON_FILE_NAME_FULL_PATH + " doesn't exist.");
        assertContent(toJson(JSON_FILE_NAME_FULL_PATH));
    }

    // QUARKUS-716
    @Test
    public void testYamlOpenApiPathAccessResource() throws IOException {
        assertTrue(Files.exists(YAML_FILE_NAME_FULL_PATH), YAML_FILE_NAME_FULL_PATH + " doesn't exist.");
        assertContent(toJson(YAML_FILE_NAME_FULL_PATH));
    }

    private JsonObject toJson(Path resourceFileName) throws IOException {
        URI resourceUri = resourceFileName.toUri();
        return (fileExtension(resourceUri.getPath()).equals(YAML)) ? fromYaml(resourceUri) : fromJson(resourceUri);
    }

    private JsonObject fromYaml(URI openApiYaml) throws IOException {
        return JsonObject.mapFrom(new Yaml().loadAs(openApiYaml.toURL().openStream(), Map.class));
    }

    private JsonObject fromJson(URI openApiJson) throws IOException {
        try (var reader = new FileReader(openApiJson.getPath())) {
            String jsonStr = IOUtils.toString(reader);
            return new JsonObject(jsonStr);
        }
    }

    private String fileExtension(String uri) {
        return FilenameUtils.getExtension(uri);
    }

    private void assertContent(JsonObject content) {
        assertThat(content.getJsonArray("tags").encode(), is(EXPECTED_TAGS));
        assertThat(content.getJsonObject("info").encode(), is(EXPECTED_INFO));
        assertTrue(content.getJsonObject("components").getJsonObject("schemas").containsKey("Score"),
                "Expected component.schema.Score object.");
        assertTrue(content.getJsonObject("paths").containsKey("/rest-ping"), "Missing expected path: /rest-ping");
        assertTrue(content.getJsonObject("paths").containsKey("/rest-pong"), "Missing expected path: /rest-pong");

        // verify that path /secured/admin is only accessible by user with role 'admin'
        var expectedRole = getRequiredRoleForPath(content, "/secured/admin");
        assertEquals("admin", expectedRole);

        // verify that path /secured/getClaimsFromBeans is accessible by any authenticated user
        expectedRole = getRequiredRoleForPath(content, "/secured/getClaimsFromBeans");
        // note: '**' is equivalent of @Authenticated and @RolesAllowed("**")
        assertEquals("**", expectedRole);

        // verify 'oidc' security schema
        var securitySchema = content
                .getJsonObject("components")
                .getJsonObject("securitySchemes")
                .getJsonObject("SecurityScheme");
        var actual = securitySchema.getString("type");
        assertEquals("openIdConnect", actual);
        actual = securitySchema.getString("description");
        assertEquals("Authentication", actual);
        actual = securitySchema.getString("openIdConnectUrl");
        assertNotNull(actual);
        assertTrue(actual.endsWith("/auth/realms/test-realm/.well-known/openid-configuration"));
    }

    private static String getRequiredRoleForPath(JsonObject content, String path) {
        var securityScheme = content
                .getJsonObject("paths")
                .getJsonObject(path)
                .getJsonObject("get")
                .getJsonArray("security")
                .getJsonObject(0)
                .getJsonArray("SecurityScheme");

        if (securityScheme.isEmpty()) {
            LOGGER.infof("There are no roles for path '%s': %s", path, content);
            return null;
        }

        return securityScheme.getString(0);
    }
}
