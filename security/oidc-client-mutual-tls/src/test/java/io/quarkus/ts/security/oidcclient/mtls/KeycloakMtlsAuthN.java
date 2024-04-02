package io.quarkus.ts.security.oidcclient.mtls;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import javax.net.ssl.SSLHandshakeException;

@Tag("QUARKUS-3466")
public abstract class KeycloakMtlsAuthN extends BaseOidcMtlsIT {

    @Test
    void verifyOidcAuthenticated() {
        verifyAuthSuccess("/oidc", NORMAL_USER + "@gmail.com");
    }

    protected void verifyAuthSuccess(String subPath, String expectedPrincipal) {
        getApp()
                .given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(getToken(NORMAL_USER))
                .get(RESOURCE_PATH + subPath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(expectedPrincipal));
    }

    protected void verifyUnauthorized(String subPath) {
        getApp()
                .given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(getToken(NORMAL_USER))
                .get(RESOURCE_PATH + subPath)
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void verifyFailsWithoutMtls() {
        assertThrows(SSLHandshakeException.class, getTokenWithoutMutualTlsHandshake());
    }

    @Test
    void verifyOidcUnauthenticated() {
        getApp().given()
                .spec(getMtlsRequestSpec()) // this line asserts OIDC mechanism is selected
                .get(RESOURCE_PATH + "/oidc")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void verifyMtlsUnauthenticated() {
        getApp().given()
                .get(RESOURCE_PATH + "/mtls/authentication")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void verifyMtlsAuthenticated() {
        verifyAuthSuccess("/mtls/authentication", getExpectedMtlsPrincipal());
    }

    private Executable getTokenWithoutMutualTlsHandshake() {
        return () -> new TokenRequest(getKeycloakService().getRealmUrl(), NORMAL_USER,
                NORMAL_USER).execute();
    }

    protected abstract RestService getApp();

    protected abstract String getExpectedMtlsPrincipal();

    protected RequestSpecification getMtlsRequestSpec() {
        var uri = getApp().getURI(Protocol.HTTPS);
        return new RequestSpecBuilder()
                .setBaseUri("%s://%s".formatted(uri.getScheme(), uri.getHost()))
                .setPort(uri.getPort())
                .setKeyStore("client-keystore." + getKeystoreFileExtension(), "password")
                .setTrustStore("client-truststore." + getKeystoreFileExtension(), "password")
                .build();
    }
}
