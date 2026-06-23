package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndNative;
import io.quarkus.test.services.KeycloakContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.FileUtils;
import io.smallrye.jwt.build.Jwt;

@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisabledOnFipsAndNative(reason = "Test uses pre-generated certificate and private key.")
public class BearerFilesystemIT {
    private static final String CLIENT_ID = "token-application-client";
    private static final String USER_USERNAME = "test-user";
    private static final String USER_PASSWORD = "test-user";

    /**
     * This is private key for keycloak-client with username "token-application-client"
     * Corresponding public-key is in keycloak "test-realm-realm.json" file in token-application-client's certificate
     */
    private static final String PRIVATE_KEY_FILE = "key.pem";
    private RSAPrivateKey privateKey = null;

    @KeycloakContainer(runKeycloakInProdMode = true)
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH);

    private static String tokenFilePath = null;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties())
            // remove OIDC credentials, that are in the application.properties
            // we want to force the app to use JWT token, not default username:password
            .withProperty("quarkus.oidc.credentials.secret", "")
            .withProperty("quarkus.oidc.client-id", "")
            // enabling introspection is required for app to actually contact the OIDC provider and use it's OIDC credentials
            .withProperty("quarkus.oidc.token.require-jwt-introspection-only", "true")
            // set bearer token config
            .withProperty("quarkus.oidc.credentials.jwt.source", "bearer")
            .withProperty("quarkus.oidc.credentials.jwt.token-path", BearerFilesystemIT::getTokenFilePath)
            .onPreStart(s -> {
                // WORKAROUND for https://github.com/quarkusio/quarkus/issues/55045
                // Quarkus eagerly validates the bearer token file at startup.
                // Write an already-expired dummy JWT so the file exists and passes startup
                // validation; the token is expired so Quarkus will re-read the file on every
                // request, which is exactly what the test scenarios need.
                // Revert this workaround once the upstream issue is resolved.
                long exp = System.currentTimeMillis() / 1000 - 1;
                String header = Base64.encodeBase64URLSafeString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
                String payload = Base64.encodeBase64URLSafeString(
                        ("{\"exp\":" + exp + "}").getBytes(StandardCharsets.UTF_8));
                FileUtils.copyContentTo(header + "." + payload + ".dummy", Path.of(getTokenFilePath()));
            });

    @Test
    @Order(1)
    public void missingTokenFileTest() throws Exception {
        // The onPreStart workaround creates a dummy token file; delete it so we can test the missing-file scenario.
        Files.deleteIfExists(Path.of(getTokenFilePath()));
        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        app.logs().assertContains("Cannot find a file with a JWT bearer token at path: " + getTokenFilePath());
    }

    @Test
    @Order(2)
    public void emptyTokenFileTest() throws Exception {
        FileUtils.copyContentTo("", Path.of(getTokenFilePath()));
        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        app.logs()
                .assertContains(
                        "JWT bearer token file at path " + getTokenFilePath() + " is empty or contains only whitespace");
    }

    @Test
    @Order(3)
    public void malformedTokenTest() throws Exception {
        FileUtils.copyContentTo("Invalid token string", Path.of(getTokenFilePath()));
        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        app.logs().assertContains("JWT bearer token or its expiry claim is invalid");
    }

    @Test
    @Order(4)
    // testing valid token needs to be last, because token will be cached
    public void validTokenTest() throws Exception {
        FileUtils.copyContentTo(createSignedJwt(), Path.of(getTokenFilePath()));

        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @AfterAll
    public static void removeTokenFile() throws IOException {
        Files.deleteIfExists(Path.of(getTokenFilePath()));
    }

    private String createSignedJwt() throws Exception {
        return createSignedJwt(keycloak.getRealmUrl(), CLIENT_ID, readPrivateKey());
    }

    private RSAPrivateKey readPrivateKey() throws Exception {
        if (privateKey == null) {
            privateKey = readPKCS8PrivateKey(
                    new File(getClass().getClassLoader().getResource(PRIVATE_KEY_FILE).getFile()).toPath());
        }
        return privateKey;
    }

    /**
     * Signed JWT is used to authenticate keycloak-client.
     * FIXME: Token is for one time-use only. Not sure how/if it can be changed.
     */
    private String createSignedJwt(String keycloakUrl, String clientId, RSAPrivateKey privateKey) {
        return Jwt.preferredUserName("test-application-client")
                .groups("Contributor")
                .issuer(clientId)
                .audience(keycloakUrl)
                .subject(clientId)
                .sign(privateKey);
    }

    /**
     * User token is used to authenticate specific user.
     * Needs to have client-authenticating token.
     */
    private String createUserToken() throws Exception {
        return given()
                .relaxedHTTPSValidation()
                .param("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                .param("client_assertion", createSignedJwt())
                .param("grant_type", "password")
                .param("username", USER_USERNAME)
                .param("password", USER_PASSWORD)
                .post(keycloak.getRealmUrl() + "/protocol/openid-connect/token")
                .jsonPath().getString("access_token");
    }

    public RSAPrivateKey readPKCS8PrivateKey(Path file) throws Exception {
        String key = Files.readString(file, Charset.defaultCharset());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static String getTokenFilePath() {
        if (tokenFilePath != null) {
            return tokenFilePath;
        }
        String tmpDir = System.getProperty("java.io.tmpdir");
        String path = RandomStringUtils.insecure().nextAlphanumeric(8) + ".token";

        tokenFilePath = Path.of(tmpDir, path).toString();
        return tokenFilePath;
    }
}
