package io.quarkus.ts.security.oidcclient.mtls;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

@Tag("QUARKUS-5663")
public abstract class AbstractOidcMtlsBindingIT {

    protected static final int KEYCLOAK_PORT = 8443;
    protected static final String REALM_DEFAULT = "test-mutual-tls-realm";
    protected static final String DEFAULT_REALM_FILE = "/test-mutual-tls-realm-realm.json";
    protected static final String NORMAL_USER = "test-normal-user";
    protected static final String CLIENT_ID = "test-mutual-tls-binding";

    protected static final String CLIENT_KEYSTORE = "client-keystore.p12";
    protected static final String CLIENT_TRUSTSTORE = "client-truststore.p12";

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication(ssl = true, properties = "mtls-binding.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperty("quarkus.oidc.client-id", CLIENT_ID);

    @Test
    void verifySuccessfulAuthentification() {
        given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(getToken(NORMAL_USER))
                .get("/mtls-binding")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString(NORMAL_USER),
                        containsString("CN=localhost,OU=QuarkusQE,O=Redhat,L=Brno,ST=BR,C=CZ"));
    }

    @Test
    void verifyUnauthorizedWithoutClientCertificate() {
        given()
                .spec(getMtlsRequestSpecWithoutClientCertificate())
                .auth().oauth2(getToken(NORMAL_USER))
                .get("/mtls-binding")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void verifyUnauthorizedWithoutToken() {
        given()
                .spec(getMtlsRequestSpec())
                .get("/mtls-binding")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void verifyUnauthorizedWithModifiedToken() {
        String token = modifyCnfValue(getToken(NORMAL_USER), 43);
        given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(token)
                .get("/mtls-binding")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void verifyUnauthorizedWithEmptyCnf() {
        String token = modifyCnfValue(getToken(NORMAL_USER), 0);
        given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(token)
                .get("/mtls-binding")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void verifyUnauthorizedWithoutCnfAttribute() {
        given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(getTokenWithoutCnf(NORMAL_USER))
                .get("/mtls-binding")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private RequestSpecification getMtlsRequestSpec() {
        var uri = app.getURI(Protocol.HTTPS);
        return new RequestSpecBuilder()
                .setBaseUri("%s://%s".formatted(uri.getScheme(), uri.getHost()))
                .setPort(uri.getPort())
                .setKeyStore(CLIENT_KEYSTORE, "password")
                .setTrustStore(CLIENT_TRUSTSTORE, "password")
                .build();
    }

    private RequestSpecification getMtlsRequestSpecWithoutClientCertificate() {
        var uri = app.getURI(Protocol.HTTPS);
        return new RequestSpecBuilder()
                .setBaseUri("%s://%s".formatted(uri.getScheme(), uri.getHost()))
                .setPort(uri.getPort())
                .setTrustStore(CLIENT_TRUSTSTORE, "password")
                .build();
    }

    private String getToken(String userName) {
        return new BaseOidcMtlsIT.TokenRequest(keycloak.getRealmUrl(), userName, userName, CLIENT_ID)
                .withKeystore(getKeyStorePath())
                .withTrustStore(getTrustStorePath())
                .execAndReturnAccessToken();
    }

    private String getTokenWithoutCnf(String userName) {
        return new BaseOidcMtlsIT.TokenRequest(keycloak.getRealmUrl(), userName, userName)
                .withKeystore(getKeyStorePath())
                .withTrustStore(getTrustStorePath())
                .execAndReturnAccessToken();
    }

    private String getTrustStorePath() {
        return Paths.get("src", "main", "resources", CLIENT_TRUSTSTORE).toAbsolutePath().toString();
    }

    private String getKeyStorePath() {
        return Paths.get("src", "main", "resources", CLIENT_KEYSTORE).toAbsolutePath().toString();
    }

    private String modifyCnfValue(String token, int newCnfValueLength) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format.");
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        String payloadJson = new String(decoder.decode(parts[1]), StandardCharsets.UTF_8);
        String cnfNewValue = RandomStringUtils.insecure().nextAlphanumeric(newCnfValueLength);
        // Replacing the part of "x5t#S256":"<43-chars-of-fingerprint>" with random generated string of 43 chars
        payloadJson = payloadJson.replaceAll("(\"x5t#S256\":\")[^\"]*(\")", "\"x5t#S256\":\"" + cnfNewValue + "\"");
        String encodedPayload = encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        return parts[0] + "." + encodedPayload + "." + parts[2];
    }
}
