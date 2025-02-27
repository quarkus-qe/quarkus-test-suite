package io.quarkus.ts.security.https.secured;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.security.certificate.ClientCertificateRequest;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

@QuarkusScenario
public class TlsRegistryCertificateReloadingIT {

    private static final String MTLS_PATH = "/secured/mtls";
    private static final String CERT_PREFIX = "qe-test";
    private static final String CLIENT_CN_1 = "client-cn-1";
    private static final String NEW_CLIENT_CN = "my-new-client";
    private static final String TLS_CONFIG_NAME = "mtls-http";

    @QuarkusApplication(ssl = true, certificates = @Certificate(clientCertificates = {
            @Certificate.ClientCertificate(cnAttribute = CLIENT_CN_1)
    }, configureTruststore = true, configureHttpServer = true, configureKeystore = true, prefix = CERT_PREFIX, format = Certificate.Format.ENCRYPTED_PEM, tlsConfigName = "mtls-http"))
    static final RestService app = new RestService()
            .withProperty("quarkus.http.ssl.client-auth", "request")
            .withProperty("quarkus.management.ssl.client-auth", "required")
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.tls." + TLS_CONFIG_NAME + ".reload-period", "2s");

    @Test
    public void testCertificateReload() {
        var response = app.mutinyHttps(CLIENT_CN_1).get("/reload-mtls-certificates").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Certificates reloaded.", response.bodyAsString());

        var clientReq = new ClientCertificateRequest(NEW_CLIENT_CN, false);
        app
                .<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .regenerateCertificate(CERT_PREFIX, certRequest -> certRequest.withClientRequests(clientReq));

        AwaitilityUtils.untilAsserted(() -> {
            var httpResponse = app.mutinyHttps(NEW_CLIENT_CN).get(MTLS_PATH).sendAndAwait();
            assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());
            assertEquals("Client certificate: CN=" + NEW_CLIENT_CN, httpResponse.bodyAsString());

        });
    }

}
