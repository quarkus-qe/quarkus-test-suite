package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.restassured.RestAssured.given;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnFips;
import io.quarkus.test.services.QuarkusApplication;
import io.smallrye.jwt.build.Jwt;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisabledOnFips(reason = "Test uses pre-generated certificate and private key.")
@Tag("QUARKUS-7867")
abstract public class AbstractSpiffeClientAuthIT {

    static final String SPIFFE_CLIENT_ID = "spiffe-application-client";
    static final String CLIENT_ID_SECRET = "test-application-client";
    static final String CLIENT_SECRET = "test-application-client-secret";
    static final String USER_USERNAME = "test-user";
    static final String USER_PASSWORD = "test-user";
    static final String PRIVATE_KEY_FILE = "key.pem";
    static final String SPIFFE_ID = "spiffe://example.org/workload";
    static final String SPIFFE_TRUST_DOMAIN = "spiffe://example.org";
    static final String SPIFFE_IDP_ALIAS = "spiffe-idp";
    static final String SPIFFE_TOKEN_PATH = System.getProperty("java.io.tmpdir") + File.separator + "spiffe-test-token";
    static final int SHORT_TOKEN_EXPIRY_SEC = 3;

    static RSAPrivateCrtKey privateKey;

    @BeforeAll
    public static void loadPrivateKey() throws Exception {
        privateKey = readPKCS8PrivateKey(
                new File(AbstractSpiffeClientAuthIT.class.getClassLoader().getResource(PRIVATE_KEY_FILE).getFile())
                        .toPath());
    }

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties())
            .withProperty("quarkus.oidc.client-id", SPIFFE_CLIENT_ID)
            .withProperty("quarkus.oidc.credentials.secret", "")
            .withProperty("quarkus.oidc.token.require-jwt-introspection-only", "true")
            .withProperty("quarkus.oidc.credentials.jwt.source", "spiffe-jwt")
            .withProperty("quarkus.oidc.credentials.jwt.token-path", SPIFFE_TOKEN_PATH)
            .withProperty("quarkus.oidc-client.client-id", SPIFFE_CLIENT_ID)
            .withProperty("quarkus.oidc-client.credentials.secret", "")
            .withProperty("quarkus.oidc-client.credentials.jwt.source", "spiffe-jwt")
            .withProperty("quarkus.oidc-client.credentials.jwt.token-path", SPIFFE_TOKEN_PATH)
            .withProperty("quarkus.oidc-client.early-tokens-acquisition", "false")
            .withProperty("spiffe.test.token-path", SPIFFE_TOKEN_PATH)
            .withProperty("spiffe.test.jwks", AbstractSpiffeClientAuthIT::computeJwks);

    @Test
    @Order(1)
    public void invalidSubClaimTest() throws Exception {
        String token = createJwtWithSub("not-a-spiffe-id", Duration.ofMinutes(5));
        updateTokenFile(token);

        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        app.logs().assertContains(
                "SPIFFE JWT-SVID token 'sub' claim is missing or does not start with 'spiffe://'");
    }

    @Test
    @Order(2)
    public void missingExpirationTest() throws Exception {
        String token = createJwtWithoutExp();
        updateTokenFile(token);

        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        app.logs().assertContains("SPIFFE JWT-SVID token or its expiry claim is invalid");
    }

    @Test
    @Order(3)
    public void missingAudienceTest() throws Exception {
        String token = Jwt.subject(SPIFFE_ID)
                .expiresIn(Duration.ofSeconds(SHORT_TOKEN_EXPIRY_SEC))
                .sign(privateKey);
        updateTokenFile(token);

        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        // wait for the short-lived token to expire so the OIDC client discards it before the next test
        Thread.sleep((SHORT_TOKEN_EXPIRY_SEC + 1) * 1000L);
    }

    @Test
    @Order(4)
    public void tokenRefreshTest() throws Exception {
        String expiredToken = Jwt.subject(SPIFFE_ID)
                .audience(keycloak.getRealmUrl())
                .expiresAt(System.currentTimeMillis() / 1000 - 60)
                .sign(privateKey);
        updateTokenFile(expiredToken);

        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        String validToken = createSpiffeJwt(Duration.ofMinutes(5));
        updateTokenFile(validToken);

        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Order(5)
    public void clientCredentialsGrantTest() throws Exception {
        updateTokenFile(createSpiffeJwt(Duration.ofMinutes(5)));

        given()
                .get("/generate-token/client-credentials")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Order(6)
    public void codeFlowAuthTest() throws Exception {
        given().auth().oauth2(createUserToken())
                .get("/secured/getClaimsFromToken")
                .then().statusCode(HttpStatus.SC_OK);
    }

    protected static void createSpiffeIdentityProvider(String bundleEndpointUrl) {
        // getRealmUrl() strips the standard port (80/443), so it cannot be used to reach the admin API: rest-assured
        // would fall back to the globally-configured application port. The Keycloak service URI keeps the real port,
        // so the absolute admin URLs below carry an explicit port and are routed correctly.
        var keycloakUri = keycloak.getURI(keycloak.getRealmUrl().startsWith("https") ? Protocol.HTTPS : Protocol.HTTP);
        String keycloakBaseUrl = keycloakUri.getScheme() + "://" + keycloakUri.getHost() + ":" + keycloakUri.getPort();
        String adminTokenEndpoint = keycloakBaseUrl + "/realms/master/protocol/openid-connect/token";
        String idpEndpoint = keycloakBaseUrl + "/admin/realms/" + DEFAULT_REALM + "/identity-provider/instances";

        String adminToken = given()
                .relaxedHTTPSValidation()
                .param("grant_type", "password")
                .param("client_id", "admin-cli")
                .param("username", "admin")
                .param("password", "admin")
                .post(adminTokenEndpoint)
                .jsonPath().getString("access_token");

        String idpBody = """
                {
                  "alias": "%s",
                  "providerId": "spiffe",
                  "enabled": true,
                  "config": {
                    "trustDomain": "%s",
                    "bundleEndpoint": "%s"
                  }
                }
                """.formatted(SPIFFE_IDP_ALIAS, SPIFFE_TRUST_DOMAIN, bundleEndpointUrl);

        given()
                .relaxedHTTPSValidation()
                .header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json")
                .body(idpBody)
                .post(idpEndpoint)
                .then().statusCode(HttpStatus.SC_CREATED);
    }

    private void updateTokenFile(String content) {
        given().header("Content-Type", "text/plain")
                .body(content)
                .post("/spiffe-test/token")
                .then().statusCode(HttpStatus.SC_OK);
    }

    private String createSpiffeJwt(Duration expiresIn) throws Exception {
        return Jwt.subject(SPIFFE_ID)
                .audience(keycloak.getRealmUrl())
                .expiresIn(expiresIn)
                .sign(privateKey);
    }

    private String createJwtWithSub(String sub, Duration expiresIn) throws Exception {
        return Jwt.subject(sub)
                .audience(keycloak.getRealmUrl())
                .expiresIn(expiresIn)
                .sign(privateKey);
    }

    private String createJwtWithoutExp() throws Exception {
        long iat = System.currentTimeMillis() / 1000;
        String headerStr = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String payloadStr = "{\"sub\":\"%s\",\"aud\":\"%s\",\"iat\":%d}".formatted(SPIFFE_ID, keycloak.getRealmUrl(), iat);

        String header = Base64.encodeBase64URLSafeString(headerStr.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.encodeBase64URLSafeString(payloadStr.getBytes(StandardCharsets.UTF_8));
        String signingInput = header + "." + payload;

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.encodeBase64URLSafeString(sig.sign());

        return signingInput + "." + signature;
    }

    private String createUserToken() {
        return keycloak.createAuthzClient(CLIENT_ID_SECRET, CLIENT_SECRET)
                .obtainAccessToken(USER_USERNAME, USER_PASSWORD).getToken();
    }

    static RSAPrivateCrtKey readPKCS8PrivateKey(Path file) throws Exception {
        String key = Files.readString(file, Charset.defaultCharset());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateCrtKey) keyFactory.generatePrivate(keySpec);
    }

    static String computeJwks() {
        try {
            RSAPrivateCrtKey key = readPKCS8PrivateKey(
                    new File(AbstractSpiffeClientAuthIT.class.getClassLoader()
                            .getResource(PRIVATE_KEY_FILE).getFile()).toPath());
            String n = Base64.encodeBase64URLSafeString(stripLeadingZero(key.getModulus()));
            String e = Base64.encodeBase64URLSafeString(stripLeadingZero(key.getPublicExponent()));
            return "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"n\":\"%s\",\"e\":\"%s\"}]}".formatted(n, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute JWKS", e);
        }
    }

    // Strips the leading 0x00 sign byte that BigInteger.toByteArray() adds for positive values with MSB set
    static byte[] stripLeadingZero(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            return tmp;
        }
        return bytes;
    }
}
