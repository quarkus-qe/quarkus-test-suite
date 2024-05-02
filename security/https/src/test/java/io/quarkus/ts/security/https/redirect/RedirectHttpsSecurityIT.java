package io.quarkus.ts.security.https.redirect;

import static io.quarkus.ts.security.https.utils.HttpsAssertions.HELLO_SIMPLE_PATH;
import static io.quarkus.ts.security.https.utils.HttpsAssertions.assertTlsHandshakeError;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.vertx.http.runtime.HttpConfiguration;
import io.vertx.ext.web.client.WebClientOptions;

@QuarkusScenario
public class RedirectHttpsSecurityIT {

    @QuarkusApplication(ssl = true, certificates = @Certificate(configureKeystore = true, configureTruststore = true))
    static RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", HttpConfiguration.InsecureRequests.REDIRECT.name());

    @Test
    public void https() {
        String response = app.mutinyHttps().get(HELLO_SIMPLE_PATH).sendAndAwait().bodyAsString();
        assertEquals("Hello, use SSL true", response);
    }

    @Test
    public void httpsServerCertificateUnknownToClient() {
        assertTlsHandshakeError(app.mutinyHttps(true, null, false).get(HELLO_SIMPLE_PATH)::sendAndAwait);
    }

    @Test
    public void httpRedirect() {
        var response = app.mutiny(new WebClientOptions().setFollowRedirects(false)).get(HELLO_SIMPLE_PATH).sendAndAwait();
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, response.statusCode());
        String expectHttpsLocation = app.getURI(Protocol.HTTPS).withPath(HELLO_SIMPLE_PATH).toString();
        assertEquals(expectHttpsLocation, response.getHeader("Location"));
    }
}
