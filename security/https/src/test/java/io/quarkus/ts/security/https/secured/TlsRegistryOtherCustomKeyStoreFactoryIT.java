package io.quarkus.ts.security.https.secured;

import static io.quarkus.test.services.Certificate.Format.PKCS12;
import static io.quarkus.ts.security.https.utils.HttpsAssertions.HELLO_SIMPLE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

/**
 * The HTTP server key store is created by a {@code KeyStoreFactory} CDI bean that the TLS registry resolves through
 * {@code quarkus.tls.<name>.key-store.other.type}.
 */
@Tag("QUARKUS-7865")
@QuarkusScenario
public class TlsRegistryOtherCustomKeyStoreFactoryIT {

    @QuarkusApplication(ssl = true, certificates = @Certificate(prefix = "other-factory", format = PKCS12, configureHttpServer = true))
    static final RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.http.ssl.client-auth", "none")
            .withProperties(() -> Map.of(
                    "quarkus.http.tls-configuration-name", "custom-factory",
                    "quarkus.tls.custom-factory.key-store.other.type", "custom-keystore",
                    "quarkus.tls.custom-factory.key-store.other.password", "password",
                    "quarkus.tls.custom-factory.key-store.other.params.key-store-path", serverKeyStorePath(),
                    "quarkus.tls.custom-factory.key-store.other.params.key-store-format", PKCS12.toString()));

    @Test
    public void testHttpsWithCustomKeyStoreFactory() {
        var response = app.mutinyHttps().get(HELLO_SIMPLE_PATH).sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Hello, use SSL true", response.bodyAsString());
    }

    private static String serverKeyStorePath() {
        return app.<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .findCertificateByPrefix("other-factory")
                .keystorePath();
    }
}
