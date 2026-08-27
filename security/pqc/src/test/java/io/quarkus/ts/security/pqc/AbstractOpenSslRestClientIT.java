package io.quarkus.ts.security.pqc;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.security.certificate.CertificateBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractOpenSslRestClientIT {

    public static final String LOG_HANDSHAKE_FAILURE_CLIENT = "routines::ssl/tls alert handshake failure";
    public static final String LOG_HANDSHAKE_FAILURE_SERVER = "Received fatal alert: handshake_failure";
    public static final String SUCCESSFUL_RESPONSE = "Hello from RESTEasy Reactive";
    public static final String CERT_PASSWORD = "password";

    @LookupService
    static RestService server;

    @LookupService
    static RestService client;

    @Test
    @Order(1)
    public void testClientStrictX25519MLKEM768AndServerStrictX25519MLKEM768() {
        setupAndRestartServer(server, "strict", "X25519MLKEM768");
        setupAndRestartClient(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(2)
    public void testClientStrictSecP256r1MLKEM768AndServerStrictX25519MLKEM768() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(3)
    public void testClientClientNegotiatedX25519MLKEM768AndServerStrictX25519MLKEM768() {
        setupAndRestartClient(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(4)
    public void testClientRelaxedAndServerStrictX25519MLKEM768() {
        setupAndRestartClient(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_SERVER);
    }

    @Test
    @Order(5)
    public void testClientStrictX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartServer(server, "client-negotiated", "X25519MLKEM768");
        setupAndRestartClient(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(6)
    public void testClientStrictSecP256r1MLKEM768AndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(7)
    public void testClientClientNegotiatedX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartClient(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(8)
    public void testClientRelaxedAndServerClientNegotiatedX25519MLKEM768() {
        setupAndRestartClient(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_SERVER);
    }

    @Test
    @Order(9)
    public void testClientStrictX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartServer(server, "client-negotiated", "X25519MLKEM768,X25519");
        setupAndRestartClient(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(10)
    public void testClientStrictSecP256r1MLKEM768AndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(11)
    public void testClientClientNegotiatedX25519MLKEM768AndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartClient(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(12)
    public void testClientRelaxedAndServerClientNegotiatedX25519MLKEM768AndX25519() {
        setupAndRestartClient(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(13)
    public void testClientStrictX25519MLKEM768AndServerRelaxed() {
        setupAndRestartServer(server, "relaxed", "X25519");
        setupAndRestartClient(client, "strict", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(14)
    public void testClientStrictSecP256r1MLKEM768AndServerRelaxed() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(15)
    public void testClientClientNegotiatedX25519MLKEM768AndServerRelaxed() {
        setupAndRestartClient(client, "client-negotiated", "X25519MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(16)
    public void testClientClientNegotiatedX25519MLKEM768AndX25519AndServerRelaxed() {
        setupAndRestartClient(client, "client-negotiated", "X25519MLKEM768,X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(17)
    public void testClientRelaxedAndServerRelaxed() {
        setupAndRestartClient(client, "relaxed", "X25519");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    private void setupAndRestartServer(RestService app, String enforcementPolicy, String keyExchangeGroups) {
        app.stop();
        app.withProperty("quarkus.tls.pqc-enforcement-policy", enforcementPolicy)
                .withProperty("quarkus.tls.key-exchange-groups", keyExchangeGroups);
        app.start();
    }

    private void setupAndRestartClient(RestService app, String enforcementPolicy, String keyExchangeGroups) {
        app.stop();
        app.withProperty("quarkus.tls.pqc.pqc-enforcement-policy", enforcementPolicy)
                .withProperty("quarkus.tls.pqc.key-exchange-groups", keyExchangeGroups);
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
