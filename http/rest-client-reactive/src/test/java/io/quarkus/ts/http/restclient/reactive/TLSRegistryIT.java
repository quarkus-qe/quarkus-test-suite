package io.quarkus.ts.http.restclient.reactive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.restclient.reactive.resources.MetaClientResource;
import io.quarkus.ts.http.restclient.reactive.resources.MetaResource;
import io.quarkus.ts.http.restclient.reactive.resources.TLSResource;
import io.restassured.response.Response;

@Tag("QUARKUS-5664")
@QuarkusScenario
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TLSRegistryIT {

    @QuarkusApplication(ssl = true, properties = "tls.properties", classes = { MetaResource.class, TLSResource.class })
    static final RestService server = new RestService()
            .setAutoStart(false);

    @QuarkusApplication(ssl = true, properties = "tls.properties", classes = { MetaClient.class, MetaClientResource.class,
            TLSResource.class })
    static final RestService client = new RestService()
            .setAutoStart(false)
            .withProperty("quarkus.rest-client.meta-client.url", () -> server.getURI(Protocol.HTTPS).toString());

    private List<String> oldLogs = Collections.emptyList();

    @Test
    @Order(1)
    // we need to check, that if trust-store.certificate-expiration-policy is not set, then it is "warn" by default
    // other tests set this property for the "client" app, so this method should be the first
    public void testDefaultPolicy() {
        server.withProperty("quarkus.http.tls-configuration-name", "expired").start();
        client.withProperty("quarkus.rest-client.meta-client.tls-configuration-name", "expired-client")
                .start();

        Response response = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(200, response.statusCode());
        List<String> headers = response.body().jsonPath().getList(".");
        assertTrue(headers.contains("User-Agent: [Quarkus REST Client]"),
                "Response wasn't processed properly. The headers are:" + headers);
        List<String> logs = client.getLogs();
        logs.removeAll(oldLogs);

        assertTrue(logContains(logs,
                "A certificate has expired: java.security.cert.CertificateExpiredException"),
                "Logs doesn't contain a warning about expired certificate! " + logs);
    }

    @Test
    public void testValidConnection() {
        server.withProperty("quarkus.http.tls-configuration-name", "server").start();
        client
                .withProperty("quarkus.rest-client.meta-client.tls-configuration-name", "client")
                .withProperty("quarkus.tls.client.trust-store.certificate-expiration-policy", "reject")
                .start();
        Response response = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(200, response.statusCode());
        List<String> headers = response.body().jsonPath().getList(".");
        assertTrue(headers.contains("User-Agent: [Quarkus REST Client]"),
                "Response wasn't processed properly. The headers are:" + headers);
    }

    @Test
    public void testIgnorePolicy() {
        server.withProperty("quarkus.http.tls-configuration-name", "expired").start();
        client
                .withProperty("quarkus.rest-client.meta-client.tls-configuration-name", "expired-client")
                .withProperty("quarkus.tls.expired-client.trust-store.certificate-expiration-policy", "ignore")
                .start();
        Response response = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(200, response.statusCode());
        List<String> headers = response.body().jsonPath().getList(".");
        assertTrue(headers.contains("User-Agent: [Quarkus REST Client]"),
                "Response wasn't processed properly. The headers are:" + headers);
        List<String> logs = client.getLogs();
        logs.removeAll(oldLogs);
        assertFalse(logContains(logs,
                "A certificate has expired"),
                "Logs contain a warning about expired certificate! " + logs);
    }

    @Test
    public void testRejectedPolicy() {
        server.withProperty("quarkus.http.tls-configuration-name", "future").start();
        client
                .withProperty("quarkus.rest-client.meta-client.tls-configuration-name", "future-client")
                .withProperty("quarkus.tls.future-client.trust-store.certificate-expiration-policy", "reject")
                .start();
        Response response = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(500, response.statusCode());
        List<String> logs = client.getLogs();
        logs.removeAll(oldLogs);
        assertTrue(logContains(logs,
                "A certificate is not yet valid - rejecting"),
                "Logs doesn't contain a warning about future validity of  certificate! " + logs);
    }

    @Test
    public void testWarningPolicy() {
        server.withProperty("quarkus.http.tls-configuration-name", "expired").start();
        client.withProperty("quarkus.rest-client.meta-client.tls-configuration-name", "expired-client")
                .withProperty("quarkus.tls.expired-client.trust-store.certificate-expiration-policy", "warn")
                .start();

        Response response = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(200, response.statusCode());
        List<String> headers = response.body().jsonPath().getList(".");
        assertTrue(headers.contains("User-Agent: [Quarkus REST Client]"),
                "Response wasn't processed properly. The headers are:" + headers);
        List<String> logs = client.getLogs();
        logs.removeAll(oldLogs);

        assertTrue(logContains(logs,
                "A certificate has expired: java.security.cert.CertificateExpiredException"),
                "Logs doesn't contain a warning about expired certificate! " + logs);
    }

    @Test
    public void testCertificateReload() {
        Path serverCerts = server.getServiceFolder().resolve("certificates").toAbsolutePath();
        Path clientCerts = client.getServiceFolder().resolve("certificates").toAbsolutePath();
        server
                .withProperty("quarkus.http.tls-configuration-name", "reloaded")
                .withProperty("quarkus.tls.reloaded.key-store.p12.path",
                        serverCerts.resolve("outdated-keystore.p12").toString())
                .start();
        client
                .withProperty("quarkus.rest-client.meta-client.tls-configuration-name", "reloaded")
                .withProperty("quarkus.tls.reloaded.trust-store.p12.path",
                        serverCerts.resolve("outdated-truststore.p12").toString())
                .withProperty("quarkus.tls.reloaded.trust-store.certificate-expiration-policy", "reject")
                .start();

        Response response = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(500, response.statusCode());
        List<String> logs = client.getLogs();
        logs.removeAll(oldLogs);

        assertTrue(logContains(logs,
                "A certificate has expired - rejecting: java.security.cert.CertificateExpiredException: NotAfter: Fri May 02"),
                "Logs doesn't contain a warning about expired certificate! " + logs);

        // reload the certs
        copyFile(serverCerts.resolve("ancient-keystore.p12"),
                serverCerts.resolve("outdated-keystore.p12"));
        Response serverReload = server.https().given().relaxedHTTPSValidation().post("/tls/reload/reloaded");
        Assertions.assertEquals(204, serverReload.statusCode());

        copyFile(clientCerts.resolve("ancient-truststore.p12"),
                clientCerts.resolve("outdated-truststore.p12"));
        Response clientReload = client.https().given().relaxedHTTPSValidation().post("/tls/reload/reloaded");
        Assertions.assertEquals(204, clientReload.statusCode());

        Response afterReload = client.https().given().relaxedHTTPSValidation()
                .get("/client/meta/headers");
        Assertions.assertEquals(500, afterReload.statusCode());
        logs = client.getLogs();
        logs.removeAll(oldLogs);
        assertTrue(logContains(logs,
                "A certificate has expired - rejecting: java.security.cert.CertificateExpiredException: NotAfter: Mon Aug 26"),
                "Logs doesn't contain a warning about expired certificate! " + logs);
    }

    public static boolean logContains(List<String> logs, String part) {
        for (String log : logs) {
            if (log.contains(part)) {
                return true;
            }
        }
        return false;
    }

    public static void copyFile(Path from, Path to) {
        Path source = from.toAbsolutePath();
        Path destination = to.toAbsolutePath();
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy file from " + source + " to " + destination, e);
        }
    }

    @BeforeEach
    public void saveLogs() {
        oldLogs = client.getLogs();
    }

    @AfterEach
    public void stopServer() {
        client.stop();
        server.stop();
    }
}
