package io.quarkus.ts.security.pqc;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.security.certificate.CertificateBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractOpenSslRestClientIT {

    public static final String LOG_HANDSHAKE_FAILURE = "routines::ssl/tls alert handshake failure";
    public static final String SUCCESSFUL_RESPONSE = "Hello from RESTEasy Reactive";
    public static final String CERT_PASSWORD = "password";

    @LookupService
    static RestService server;

    @LookupService
    static RestService client;

    @Test
    @Order(1)
    public void testClientStrictX25519MLKEM768AndServerStrictX25519MLKEM768() {
        setupAndRestartApp(server, "strict", "X25519MLKEM768");
        setupAndRestartApp(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(2)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientStrictSecP256r1MLKEM768AndServerStrictX25519MLKEM768() {
        setupAndRestartApp(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(3)
    public void testClientClientNegotiatedX25519MLKEM768AndServerStrictX25519MLKEM768() {
        setupAndRestartApp(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(4)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientRelaxedAndServerStrictX25519MLKEM768() {
        setupAndRestartApp(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(5)
    public void testClientStrictX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartApp(server, "client-negotiated", "X25519MLKEM768");
        setupAndRestartApp(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(6)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientStrictSecP256r1MLKEM768AndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartApp(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(7)
    public void testClientClientNegotiatedX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartApp(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(8)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientRelaxedAndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartApp(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(9)
    public void testClientStrictX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartApp(server, "client-negotiated", "X25519MLKEM768,X25519");
        setupAndRestartApp(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(10)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientStrictSecP256r1MLKEM768AndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartApp(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(11)
    public void testClientClientNegotiatedX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartApp(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(12)
    public void testClientRelaxedAndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartApp(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(13)
    public void testClientStrictX25519MLKEM768AndServerRelaxed() {
        setupAndRestartApp(server, "relaxed", "X25519");
        setupAndRestartApp(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(14)
    public void testClientStrictSecP256r1MLKEM768AndServerRelaxed() {
        setupAndRestartApp(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(15)
    public void testClientClientNegotiatedX25519MLKEM768AndServerRelaxed() {
        setupAndRestartApp(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE);
    }

    @Test
    @Order(16)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientClientNegotiatedX25519MLKEM768AndX25519AndServerRelaxed() {
        setupAndRestartApp(client, "client-negotiated", "X25519MLKEM768,X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(17)
    @Disabled("https://github.com/quarkusio/quarkus/issues/56262")
    public void testClientRelaxedAndServerRelaxed() {
        setupAndRestartApp(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    private void setupAndRestartApp(RestService app, String enforcementPolicy, String keyExchangeGroups) {
        app.stop();
        app.withProperty("quarkus.tls.pqc-enforcement-policy", enforcementPolicy)
                .withProperty("quarkus.tls.key-exchange-groups", keyExchangeGroups);
        app.start();
    }

    public static String serverTruststorePath() {
        CertificateBuilder certificateBuilder = (CertificateBuilder) server
                .getPropertyFromContext("io.quarkus.test.security.certificate#INSTANCE");
        if (certificateBuilder == null) {
            throw new IllegalStateException("Certificates should be available");
        }

        var certificate = certificateBuilder.certificates().get(0);
        if (certificate == null) {
            throw new IllegalStateException("Certificate should be available");
        }

        return certificate.truststorePath();
    }
}
