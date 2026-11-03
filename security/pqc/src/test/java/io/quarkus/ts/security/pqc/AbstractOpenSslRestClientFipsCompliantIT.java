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
public abstract class AbstractOpenSslRestClientFipsCompliantIT {

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
    public void testClientStrictSecP384r1MLKEM1024AndServerStrictSecP384r1MLKEM1024() {
        setupAndRestartServer(server, "strict", "SecP384r1MLKEM1024");
        setupAndRestartClient(client, "strict", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(2)
    public void testClientStrictSecP256r1MLKEM768AndServerStrictSecP384r1MLKEM1024() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(3)
    public void testClientClientNegotiatedSecP384r1MLKEM1024AndServerStrictSecP384r1MLKEM1024() {
        setupAndRestartClient(client, "client-negotiated", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(4)
    public void testClientRelaxedAndServerStrictSecP384r1MLKEM1024() {
        setupAndRestartClient(client, "relaxed", "Secp384r1");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_SERVER);
    }

    @Test
    @Order(5)
    public void testClientStrictSecP384r1MLKEM1024AndServerClientNegotiatedSecP384r1MLKEM1024() {
        setupAndRestartServer(server, "client-negotiated", "SecP384r1MLKEM1024");
        setupAndRestartClient(client, "strict", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(6)
    public void testClientStrictSecP256r1MLKEM768AndServerClientNegotiatedSecP384r1MLKEM1024() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(7)
    public void testClientClientNegotiatedSecP384r1MLKEM1024AndServerClientNegotiatedSecP384r1MLKEM1024() {
        setupAndRestartClient(client, "client-negotiated", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(8)
    public void testClientRelaxedAndServerClientNegotiatedSecP384r1MLKEM1024() {
        setupAndRestartClient(client, "relaxed", "Secp384r1");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_SERVER);
    }

    @Test
    @Order(9)
    public void testClientStrictSecP384r1MLKEM1024AndServerClientNegotiatedSecP384r1MLKEM1024AndSecp384r1() {
        setupAndRestartServer(server, "client-negotiated", "SecP384r1MLKEM1024,Secp384r1");
        setupAndRestartClient(client, "strict", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(10)
    public void testClientStrictSecP256r1MLKEM768AndServerClientNegotiatedSecP384r1MLKEM1024AndSecp384r1() {
        setupAndRestartClient(client, "strict", "SecP256r1MLKEM768");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(11)
    public void testClientClientNegotiatedSecP384r1MLKEM1024AndServerClientNegotiatedSecP384r1MLKEM1024AndSecp384r1() {
        setupAndRestartClient(client, "client-negotiated", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(12)
    public void testClientRelaxedAndServerClientNegotiatedSecP384r1MLKEM1024AndSecp384r1() {
        setupAndRestartClient(client, "relaxed", "Secp384r1");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(13)
    public void testClientStrictSecP384r1MLKEM1024AndServerRelaxed() {
        setupAndRestartServer(server, "relaxed", "Secp384r1");
        setupAndRestartClient(client, "strict", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(14)
    public void testClientStrictSecP256r1MLKEM768AndServerRelaxed() {
        setupAndRestartClient(client, "strict", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(15)
    public void testClientClientNegotiatedSecP384r1MLKEM1024AndServerRelaxed() {
        setupAndRestartClient(client, "client-negotiated", "SecP384r1MLKEM1024");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
        client.logs().assertContains(LOG_HANDSHAKE_FAILURE_CLIENT);
    }

    @Test
    @Order(16)
    public void testClientClientNegotiatedSecP384r1MLKEM1024AndSecp384r1AndServerRelaxed() {
        setupAndRestartClient(client, "client-negotiated", "SecP384r1MLKEM1024,Secp384r1");
        client.given()
                .get("/client/hello")
                .then()
                .statusCode(SC_OK)
                .body(is(SUCCESSFUL_RESPONSE));
    }

    @Test
    @Order(17)
    public void testClientRelaxedAndServerRelaxed() {
        setupAndRestartClient(client, "relaxed", "Secp384r1");
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
