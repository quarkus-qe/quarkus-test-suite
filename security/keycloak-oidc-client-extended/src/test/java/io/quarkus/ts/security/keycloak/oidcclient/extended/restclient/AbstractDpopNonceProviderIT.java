package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.oidc.common.runtime.OidcCommonUtils;
import io.quarkus.oidc.runtime.OidcUtils;
import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.URILike;
import io.restassured.response.Response;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.build.JwtSignatureBuilder;
import io.smallrye.jwt.util.KeyUtils;

@Tag("QUARKUS-7340")
abstract public class AbstractDpopNonceProviderIT {
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-user";
    private static final String CLIENT_ID = "test-application-client";
    private static final String CLIENT_SECRET = "test-application-client-secret";

    private static final String DPOP_NONCE = "DPoP-Nonce";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String VALID_NONCE = "nonce-from-provider";
    private static final String INVALID_NONCE = "invalid-nonce";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.token.authorization-scheme", "dpop")
            .withProperty("dpop.nonce.provider.enabled", "true")
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void validNonceShouldAllowAccess() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        Response response = given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "GET", "/dpop", VALID_NONCE))
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .thenReturn();

        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Hello, " + USERNAME, response.asString(), "Response should contain username");
    }

    @Test
    public void missingNonceShouldReturnNonceChallenge() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "GET", "/dpop", null))
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .header(DPOP_NONCE, VALID_NONCE)
                .header(CACHE_CONTROL, containsString("no-store"))
                .header("WWW-Authenticate", containsString("dpop error=\"use_dpop_nonce\""));
    }

    @Disabled("https://github.com/quarkusio/quarkus/pull/54855 is merged in main branch (for Quarkus 4.0), enable after it is backported to 3.x branch.")
    @Test
    public void invalidNonceShouldReturnUseDpopNonceChallenge() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "GET", "/dpop", INVALID_NONCE))
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .header(DPOP_NONCE, VALID_NONCE)
                .header(CACHE_CONTROL, containsString("no-store"))
                .header("WWW-Authenticate", containsString("dpop error=\"use_dpop_nonce\""))
                .header("WWW-Authenticate", not(containsString("invalid_dpop_proof")));
    }

    @Test
    public void clientShouldSucceedAfterRetryWithReturnedNonce() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        Response challengeResponse = given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "GET", "/dpop", null))
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .thenReturn();

        assertEquals(HttpStatus.SC_UNAUTHORIZED, challengeResponse.statusCode());

        String returnedNonce = challengeResponse.header(DPOP_NONCE);
        assertNotNull(returnedNonce);

        Response retryResponse = given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "GET", "/dpop", returnedNonce))
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .thenReturn();

        assertEquals(HttpStatus.SC_OK, retryResponse.statusCode());
        assertEquals("Hello, " + USERNAME, retryResponse.asString(), "Response should contain username");
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        return KeyUtils.generateKeyPair(2048);
    }

    private String getAccessToken(KeyPair keyPair) {
        return given()
                .relaxedHTTPSValidation()
                .header("DPOP", createDPopProofForKeycloak(keyPair))
                .param("client_id", CLIENT_ID)
                .param("client_secret", CLIENT_SECRET)
                .param("grant_type", "password")
                .param("username", USERNAME)
                .param("password", PASSWORD)
                .post(getKeycloakRealmUrl() + "/protocol/openid-connect/token")
                .jsonPath().getString("access_token");
    }

    private String createDPopProofForKeycloak(KeyPair keyPair) {
        return Jwt.claim("htm", "POST")
                .claim("htu", keycloak.getRealmUrl() + "/protocol/openid-connect/token")
                .jws()
                .header("typ", "dpop+jwt")
                .jwk(keyPair.getPublic())
                .sign(keyPair.getPrivate());
    }

    private String createDPopProofForQuarkus(KeyPair keyPair, String accessToken, String httpMethod, String dPopEndpointPath,
            String nonce)
            throws Exception {

        URILike uriLike = app.getURI(Protocol.HTTP);
        String uri = "http://" + uriLike.getHost();
        /*
         * Quarkus drop default http port 80 from URI when validating DPoP proof.
         * So if string ":80" is in DPoP proof it will cause a mismatch in proof validation.
         * But for any other port, it has to be present
         */
        if (uriLike.getPort() != 80) {
            uri += ":" + uriLike.getPort();
        }

        JwtClaimsBuilder jwtClaimsBuilder = Jwt.claim("htm", httpMethod)
                .claim("htu", uri + dPopEndpointPath);

        if (nonce != null) {
            jwtClaimsBuilder.claim("nonce", nonce);
        }

        JwtSignatureBuilder jwtSignatureBuilder = jwtClaimsBuilder
                .claim("ath", OidcCommonUtils.base64UrlEncode(
                        OidcUtils.getSha256Digest(accessToken)))
                .jws()
                .jwk(keyPair.getPublic())
                .header("typ", "dpop+jwt");

        return jwtSignatureBuilder.sign(keyPair.getPrivate());
    }

    protected String getKeycloakRealmUrl() {
        return keycloak.getRealmUrl();
    }
}
