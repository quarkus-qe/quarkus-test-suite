package io.quarkus.ts.security.https.secured;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.security.certificate.ClientCertificateRequest;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

/**
 * Certificate reload of an {@code other}-typed key store and trust store loaded by the standard fallback, that is
 * {@code KeyStore.getInstance(type)} over the configured path. Both stores are regenerated on disk and the reload is
 * triggered explicitly, then the new certificates are verified over a live mutual TLS connection.
 */
@Tag("QUARKUS-7865")
@QuarkusScenario
public class TlsRegistryOtherKeyStoreReloadingIT {

    private static final String MTLS_PATH = "/secured/mtls";
    private static final String CERT_PREFIX = "other-reload";
    private static final String CLIENT_CN = "other-reload-client";
    private static final String NEW_CLIENT_CN = "other-reload-new-client";
    private static final String TLS_CONFIG_NAME = "other-reload-http";

    @QuarkusApplication(ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, format = Certificate.Format.PKCS12, tlsConfigName = TLS_CONFIG_NAME, configureHttpServer = true, clientCertificates = @Certificate.ClientCertificate(cnAttribute = CLIENT_CN)))
    static final RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.http.ssl.client-auth", "required")
            .withProperties(() -> Map.of(
                    "quarkus.tls." + TLS_CONFIG_NAME + ".key-store.other.type", "PKCS12",
                    "quarkus.tls." + TLS_CONFIG_NAME + ".key-store.other.path", certificate().keystorePath(),
                    "quarkus.tls." + TLS_CONFIG_NAME + ".key-store.other.password", "password",
                    "quarkus.tls." + TLS_CONFIG_NAME + ".trust-store.other.type", "PKCS12",
                    "quarkus.tls." + TLS_CONFIG_NAME + ".trust-store.other.path", certificate().truststorePath(),
                    "quarkus.tls." + TLS_CONFIG_NAME + ".trust-store.other.password", "password"));

    @Test
    public void testCertificateReload() {
        var clientBeforeReload = app.mutinyHttps(CLIENT_CN);
        var responseBeforeReload = clientBeforeReload.get(MTLS_PATH).sendAndAwait();
        assertEquals(HttpStatus.SC_OK, responseBeforeReload.statusCode());
        assertEquals("Client certificate: CN=" + CLIENT_CN, responseBeforeReload.bodyAsString());

        app.<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .regenerateCertificate(CERT_PREFIX,
                        certRequest -> {
                            certRequest.withClientRequests(new ClientCertificateRequest(NEW_CLIENT_CN, false));
                        });

        var responseReload = clientBeforeReload
                .get("/reload-mtls-certificates?tls-configuration-name=" + TLS_CONFIG_NAME).sendAndAwait();
        assertEquals(HttpStatus.SC_OK, responseReload.statusCode());
        assertEquals("Certificates reloaded.", responseReload.bodyAsString());

        AwaitilityUtils.untilAsserted(() -> {
            var responseAfterReload = app.mutinyHttps(NEW_CLIENT_CN).get(MTLS_PATH).sendAndAwait();
            assertEquals(HttpStatus.SC_OK, responseAfterReload.statusCode());
            assertEquals("Client certificate: CN=" + NEW_CLIENT_CN, responseAfterReload.bodyAsString());
        });

        // This still works because the client is reusing the cached HTTP/TLS connection established before the reload
        responseBeforeReload = clientBeforeReload.get(MTLS_PATH).sendAndAwait();
        assertEquals(HttpStatus.SC_OK, responseBeforeReload.statusCode());

        // Fail when trying to create a new client with the old certificate
        assertThrows(RuntimeException.class, () -> app.mutinyHttps(CLIENT_CN));
    }

    private static io.quarkus.test.security.certificate.Certificate certificate() {
        return app.<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .findCertificateByPrefix(CERT_PREFIX);
    }
}
