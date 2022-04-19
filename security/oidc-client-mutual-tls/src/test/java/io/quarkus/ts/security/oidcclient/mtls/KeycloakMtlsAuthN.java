package io.quarkus.ts.security.oidcclient.mtls;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public abstract class KeycloakMtlsAuthN extends BaseOidcMtlsIT {

    @Test
    void verifyAuthenticated() {
        given()
                .when()
                .auth().oauth2(getToken(NORMAL_USER))
                .get(RESOURCE_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(NORMAL_USER + "@gmail.com"));
    }

    @Test
    void verifyFailsWithoutMtls() {
        assertThrows(SSLHandshakeException.class, getTokenWithoutMutualTlsHandshake());
    }

    @Test
    void verifyUnauthenticated() {
        given()
                .when()
                .get(RESOURCE_PATH)
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private Executable getTokenWithoutMutualTlsHandshake() {
        return () -> new TokenRequest(getKeycloakService().getRealmUrl(), NORMAL_USER,
                NORMAL_USER).execute();
    }

}
