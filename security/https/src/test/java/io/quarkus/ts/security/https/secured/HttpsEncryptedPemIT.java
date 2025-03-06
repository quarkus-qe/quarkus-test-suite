package io.quarkus.ts.security.https.secured;

import static io.quarkus.ts.security.https.utils.Certificates.CLIENT_CN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class HttpsEncryptedPemIT {
    @QuarkusApplication(ssl = true, certificates = @Certificate(format = Certificate.Format.ENCRYPTED_PEM, configureHttpServer = true, clientCertificates = {
            @Certificate.ClientCertificate(cnAttribute = CLIENT_CN) }))
    static final RestService app = new RestService()
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.http.ssl.client-auth", "none");

    @Test
    public void simpleHttpsCommunicationEncryptedPem() {
        var response = app.mutinyHttps(CLIENT_CN).get("/hello/simple").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Hello, use SSL true", response.bodyAsString(),
                "Response is not the expected on that endpoint");
    }
}
