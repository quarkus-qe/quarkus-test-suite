package io.quarkus.ts.security.https.secured;

import static io.quarkus.ts.security.https.utils.Certificates.CLIENT_CN;
import static io.quarkus.ts.security.https.utils.Certificates.CLIENT_PASSWORD;
import static io.quarkus.ts.security.https.utils.Certificates.GUEST_CLIENT_CN;
import static io.quarkus.ts.security.https.utils.Certificates.UNKNOWN_CLIENT_CN;
import static io.quarkus.ts.security.https.utils.HttpsAssertions.assertTlsHandshakeError;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-3466")
@QuarkusScenario
public class AuthzHttpsSecurityIT {

    private static final String SECURED_PATH = "/secured";
    private static final String HELLO_FULL_PATH = "/hello/full";

    @QuarkusApplication(ssl = true, certificates = @Certificate(useTlsRegistry = false, configureHttpServer = true, configureKeystore = true, configureTruststore = true, password = CLIENT_PASSWORD, clientCertificates = {
            @Certificate.ClientCertificate(cnAttribute = CLIENT_CN),
            @Certificate.ClientCertificate(cnAttribute = GUEST_CLIENT_CN),
            @Certificate.ClientCertificate(cnAttribute = UNKNOWN_CLIENT_CN, unknownToServer = true)
    }))
    static RestService app = new RestService();

    @Test
    public void httpsAuthenticatedAndAuthorizedClient() {
        var webClient = app.mutinyHttps(CLIENT_CN);

        var response = webClient.get(HELLO_FULL_PATH).sendAndAwait().bodyAsString();
        assertEquals("Hello CN=client, HTTPS: true, isUser: true, isGuest: false", response);

        response = webClient.get(SECURED_PATH).sendAndAwait().bodyAsString();
        assertEquals("Client certificate: CN=client", response);
    }

    @Test
    public void httpsAuthenticatedButUnauthorizedClient() {
        var webClient = app.mutinyHttps(GUEST_CLIENT_CN);

        var response = webClient.get(HELLO_FULL_PATH).sendAndAwait().bodyAsString();
        assertEquals("Hello CN=guest-client, HTTPS: true, isUser: false, isGuest: true", response);

        var statusCode = webClient.get(SECURED_PATH).sendAndAwait().statusCode();
        assertEquals(HttpStatus.SC_FORBIDDEN, statusCode);
    }

    @Test
    public void httpsServerCertificateUnknownToClient() {
        assertTlsHandshakeError(app.mutinyHttps(true, CLIENT_CN, false).get(HELLO_FULL_PATH)::sendAndAwait);
    }

    @Test
    @DisabledOnNative(reason = "Takes too much time to validate this test on Native")
    public void httpsClientCertificateUnknownToServer() {
        var webClient = app.mutinyHttps(UNKNOWN_CLIENT_CN);

        assertTlsHandshakeError(webClient.get(HELLO_FULL_PATH)::sendAndAwait);
        assertTlsHandshakeError(webClient.get(SECURED_PATH)::sendAndAwait);
    }

    @Test
    public void httpsServerCertificateUnknownToClientClientCertificateUnknownToServer() {
        assertTlsHandshakeError(app.mutinyHttps(true, UNKNOWN_CLIENT_CN, false).get(HELLO_FULL_PATH)::sendAndAwait);
    }

    @Test
    public void http() {
        var response = app.mutiny().get(HELLO_FULL_PATH).sendAndAwait().bodyAsString();
        assertEquals("Hello <anonymous>, HTTPS: false, isUser: false, isGuest: false", response);
    }
}
