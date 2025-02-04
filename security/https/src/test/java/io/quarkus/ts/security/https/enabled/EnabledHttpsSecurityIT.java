package io.quarkus.ts.security.https.enabled;

import static io.quarkus.ts.security.https.utils.Certificates.CLIENT_CN;
import static io.quarkus.ts.security.https.utils.Certificates.CLIENT_PASSWORD;
import static io.quarkus.ts.security.https.utils.Certificates.UNKNOWN_CLIENT_CN;
import static io.quarkus.ts.security.https.utils.HttpsAssertions.HELLO_SIMPLE_PATH;
import static io.quarkus.ts.security.https.utils.HttpsAssertions.assertTlsHandshakeError;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.vertx.http.runtime.VertxHttpConfig;

@QuarkusScenario
public class EnabledHttpsSecurityIT {

    @QuarkusApplication(ssl = true, certificates = {
            @Certificate(configureKeystore = true, configureTruststore = true, useTlsRegistry = false, configureHttpServer = true, password = CLIENT_PASSWORD, clientCertificates = {
                    @Certificate.ClientCertificate(cnAttribute = CLIENT_CN),
                    @Certificate.ClientCertificate(cnAttribute = UNKNOWN_CLIENT_CN, unknownToServer = true)
            })
    })
    static RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", VertxHttpConfig.InsecureRequests.ENABLED.name());

    @Test
    public void https() {
        String response = app.mutinyHttps(CLIENT_CN).get(HELLO_SIMPLE_PATH).sendAndAwait().bodyAsString();
        assertEquals("Hello, use SSL true", response);
    }

    @Test
    public void httpsServerCertificateUnknownToClient() {
        assertTlsHandshakeError(app.mutinyHttps(true, CLIENT_CN, false).get(HELLO_SIMPLE_PATH)::sendAndAwait);
    }

    @Test
    @DisabledOnNative(reason = "Takes too much time to validate this test on Native")
    public void httpsClientCertificateUnknownToServer() {
        assertTlsHandshakeError(app.mutinyHttps(UNKNOWN_CLIENT_CN).get(HELLO_SIMPLE_PATH)::sendAndAwait);
    }

    @Test
    public void httpsServerCertificateUnknownToClientClientCertificateUnknownToServer() {
        assertTlsHandshakeError(app.mutinyHttps(true, UNKNOWN_CLIENT_CN, false).get(HELLO_SIMPLE_PATH)::sendAndAwait);
    }

    @Test
    public void http() {
        String response = app.mutiny().get(HELLO_SIMPLE_PATH).sendAndAwait().bodyAsString();
        assertEquals("Hello, use SSL false", response);
    }
}
