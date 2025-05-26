package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpStatus;
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

@Tag("https://issues.redhat.com/browse/QUARKUS-5858")
abstract public class AbstractDpopIT {
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-user";

    private static final String CLIENT_ID = "test-application-client";
    private static final String CLIENT_SECRET = "test-application-client-secret";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.token.authorization-scheme", "dpop");

    @Test
    public void correctAccessTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        Response response = given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "/dpop"))
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .thenReturn();

        assertEquals(HttpStatus.SC_OK, response.statusCode(), "Http response should be 200");
        assertEquals("Hello, " + USERNAME, response.asString(), "Response should contain username");
    }

    // test that DPoP works correctly with HTTP POST method
    @Test
    public void postMethodTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        Response response = given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "POST", "/dpop"))
                .header("Authorization", "DPoP " + accessToken)
                .post("/dpop")
                .thenReturn();

        assertEquals(HttpStatus.SC_OK, response.statusCode(), "Http response should be 200");
        assertEquals("Hello, " + USERNAME, response.asString(), "Response should contain username");
    }

    @Test
    public void jwtAuthorizationTest() {
        String accessToken = keycloak.createAuthzClient(CLIENT_ID, CLIENT_SECRET).obtainAccessToken(USERNAME, PASSWORD)
                .getToken();

        // App should require DPoP for authorization - normal access token should not work
        given()
                .header("Authorization", accessToken)
                .get("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void missingDPoPHeaderTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        given()
                .header("Authorization", "DPoP " + accessToken)
                .get("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void malformedAuthorizationHeaderTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "/dpop"))
                .header("Authorization", "DPoP invalidToken" + accessToken)
                .get("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void DPoPHeaderSignedWithWrongKeyTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        given()
                .header("DPoP", createDPopProofForQuarkus(generateKeyPair(), accessToken, "/dpop"))
                .header("Authorization", "DPoP" + accessToken)
                .get("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void mismatchingHttpEndpointTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "/anotherEndpoint"))
                .header("Authorization", "DPoP" + accessToken)
                .get("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void mismatchingHttpMethodTest() throws Exception {
        KeyPair keyPair = generateKeyPair();
        String accessToken = getAccessToken(keyPair);

        // by default proofForQuarkus is signed for GET method
        given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "/dpop"))
                .header("Authorization", "DPoP" + accessToken)
                .post("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);

        given()
                .header("DPoP", createDPopProofForQuarkus(keyPair, accessToken, "POST", "/dpop"))
                .header("Authorization", "DPoP" + accessToken)
                .get("/dpop")
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        return KeyUtils.generateKeyPair(2048);
    }

    private String getAccessToken(KeyPair keyPair) {
        return given()
                .header("DPOP", createDPopProofForKeycloak(keyPair))
                .param("client_id", CLIENT_ID)
                .param("client_secret", CLIENT_SECRET)
                .param("grant_type", "password")
                .param("username", USERNAME)
                .param("password", PASSWORD)
                .post(keycloak.getRealmUrl() + "/protocol/openid-connect/token")
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

    private String createDPopProofForQuarkus(KeyPair keyPair, String accessToken, String dPopEndpointPath) throws Exception {
        return createDPopProofForQuarkus(keyPair, accessToken, "GET", dPopEndpointPath);
    }

    private String createDPopProofForQuarkus(KeyPair keyPair, String accessToken, String httpMethod, String dPopEndpointPath)
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
        JwtSignatureBuilder jwtSignatureBuilder = jwtClaimsBuilder
                .claim("ath", OidcCommonUtils.base64UrlEncode(
                        OidcUtils.getSha256Digest(accessToken)))
                .jws()
                .jwk(keyPair.getPublic())
                .header("typ", "dpop+jwt");
        return jwtSignatureBuilder.sign(keyPair.getPrivate());
    }
}
