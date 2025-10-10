package io.quarkus.ts.security.keycloak;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MetaEndpointIT {

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);
    static final String CUSTOM_ENDPOINT = "/custom-endpoint";
    private static final String DEFAULT_META_ENDPOINT = ".well-known/oauth-protected-resource";
    static final String OVERLOADED_META_ENDPOINT = "/" + DEFAULT_META_ENDPOINT + CUSTOM_ENDPOINT;

    @QuarkusApplication
    static RestService http = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.resource-metadata.enabled", "true")
            .withProperty("quarkus.oidc.client-id", BaseOidcSecurityIT.CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", BaseOidcSecurityIT.CLIENT_SECRET_DEFAULT)
            .withProperties(keycloak::getTlsProperties);

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureTruststore = true, configureHttpServer = true))
    static RestService https = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.resource-metadata.enabled", "true")
            .withProperty("quarkus.http.ssl.client-auth", "request")
            .withProperty("quarkus.http.auth.permission.auth.policy", "authenticated")
            .withProperty("quarkus.http.auth.permission.auth.paths", "/user")
            .withProperty("quarkus.oidc.resource-metadata.resource", CUSTOM_ENDPOINT)
            .withProperty("quarkus.oidc.client-id", BaseOidcSecurityIT.CLIENT_ID_DEFAULT)
            .withProperty("quarkus.oidc.credentials.secret", BaseOidcSecurityIT.CLIENT_SECRET_DEFAULT)
            .withProperties(keycloak::getTlsProperties);

    @Test
    @Order(1)
    public void httpHasHTTPSMetadata() {
        Response response = http.given()
                .when()
                .get(DEFAULT_META_ENDPOINT);
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        JsonPath jsonPath = response.body().jsonPath();
        assertEquals(getAppURL(http, Protocol.HTTP).withScheme("https").toString(),
                jsonPath.getString("resource"),
                "No app URL in the body: " + response.body().asString());
        assertEquals(keycloak.getRealmUrl(),
                jsonPath.getString("authorization_servers[0]"),
                "No authorization server URL in the body: " + response.body().asString());
    }

    @Test
    @Order(2)
    public void noForcedHttps() {
        http.stop();
        http.withProperty("quarkus.oidc.resource-metadata.force-https-scheme", "false");
        http.start();
        Response response = http.given()
                .when()
                .get(DEFAULT_META_ENDPOINT);
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        JsonPath jsonPath = response.body().jsonPath();
        assertEquals(getAppURL(http, Protocol.HTTP).toString(),
                jsonPath.getString("resource"),
                "No app URL in the body: " + response.body().asString());
        assertEquals(keycloak.getRealmUrl(),
                jsonPath.getString("authorization_servers[0]"),
                "No authorization server URL in the body: " + response.body().asString());
    }

    @Test
    public void endpointInfoInHeader() {
        Response response = https
                .relaxedHttps()
                .given()
                .when()
                .get("/user");
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.statusCode());
        String header = response.header("www-authenticate");
        Assertions.assertNotNull(header, "There is no authentication header in the answer!");
        String metadataURL = getAppURL(https, Protocol.HTTPS)
                .withPath(OVERLOADED_META_ENDPOINT)
                .toString();
        // What value of the 'www-authenticate' header looks like:
        // Bearer resource_metadata="https://localhost:1103/.well-known/oauth-protected-resource/custom-endpoint"
        assertEquals(metadataURL,
                header.split("=")[1].replaceAll("\"", ""),
                "Authentication header doesn't contain metadata endpoint");
    }

    URILike getAppURL(RestService app, Protocol protocol) {
        return app.getURI(protocol);
    }

    @Test
    public void httpsHasMetadata() {
        Response response = https
                .relaxedHttps()
                .given()
                .when()
                .get(OVERLOADED_META_ENDPOINT);
        JsonPath jsonPath = response.body().jsonPath();
        assertEquals(getAppURL(https, Protocol.HTTPS).withPath(CUSTOM_ENDPOINT).toString(),
                jsonPath.getString("resource"),
                "No app URL in the body: " + response.body().asString());
        assertEquals(keycloak.getRealmUrl(),
                jsonPath.getString("authorization_servers[0]"),
                "No authorization server URL in the body: " + response.body().asString());
    }
}
