package io.quarkus.ts.security.https.secured;

import static io.quarkus.ts.security.https.utils.Certificates.DUMMY_ENTRY_CERT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@Tag("fips-incompatible") // Reported in https://github.com/quarkusio/quarkus/issues/46696
@QuarkusScenario
public class TlsRegistryDecryptedKeyIT {
    @QuarkusApplication(ssl = true, certificates = @Certificate(format = Certificate.Format.ENCRYPTED_PEM, configureHttpServer = true, clientCertificates = @Certificate.ClientCertificate(cnAttribute = DUMMY_ENTRY_CERT)))
    static final RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.http.ssl.client-auth", "none");

    @Test
    public void testInspectDecryptedKey() {
        var response = app.mutinyHttps(DUMMY_ENTRY_CERT).get("/tls-registry").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());

        String body = response.bodyAsString();
        assertTrue(body.contains("Subject X500 : CN=dummy-entry-0"),
                "Response from /tls-inspection does not contain subject info: " + body);
        assertTrue(body.contains("localhost") || body.contains(" [[2, localhost], [2, 0.0.0.0]]"),
                "Response from /tls-inspection does not mention Subject Alternative names (SANs) : localhost or  [[2, localhost], [2, 0.0.0.0]]");
    }
}
