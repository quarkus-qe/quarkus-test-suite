package io.quarkus.ts.security.https.secured;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.security.certificate.ClientCertificateRequest;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.Certificate.ClientCertificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.utils.AwaitilityUtils;

@Tag("QUARKUS-4592")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
public class MutualTlsSecurityIT {

    private static final String MTLS_PATH = "/secured/mtls";
    private static final String CERT_PREFIX = "qe-test";
    private static final String CLIENT = "client";
    private static final String GUEST_CLIENT = "guest-client";

    @QuarkusApplication(ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, clientCertificates = {
            @ClientCertificate(cnAttribute = CLIENT),
            @ClientCertificate(cnAttribute = GUEST_CLIENT, unknownToServer = true)
    }, configureKeystore = true, configureTruststore = true, tlsConfigName = "mtls-http", configureHttpServer = true))
    static final RestService app = new RestService()
            .withProperty("quarkus.http.auth.proactive", "false")
            .withProperty("quarkus.http.ssl.client-auth", "required")
            .withProperty("quarkus.http.insecure-requests", "disabled");

    @Order(1)
    @Test
    public void testMutualTlsForHttpServer() {
        var client1 = app.mutinyHttps(CLIENT);
        var httpResponse = client1.get(MTLS_PATH).sendAndAwait();
        assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());
        assertEquals("Client certificate: CN=" + CLIENT, httpResponse.bodyAsString());

        callSecuredEndpointAndExpectFailure(GUEST_CLIENT);
    }

    @Order(2)
    @Test
    public void testCertificateReloading() {
        // important to load before certificates are reloaded because otherwise
        // we would have new invalid certificate on test side and expected different certs on the server side
        var clientBeforeReload = app.mutinyHttps(CLIENT);

        app
                .<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .regenerateCertificate(CERT_PREFIX, certRequest -> {
                    // regenerate client certificates
                    // make the first client invalid
                    var clientOneCert = new ClientCertificateRequest(CLIENT, true);
                    // make the second client valid
                    var clientTwoCert = new ClientCertificateRequest(GUEST_CLIENT, false);
                    certRequest.withClientRequests(clientOneCert, clientTwoCert);
                });

        var response = clientBeforeReload.get("/reload-mtls-certificates").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Certificates reloaded.", response.bodyAsString());

        // now we expect opposite from what we tested in step one: client 1 must fail and client 2 must succeed
        AwaitilityUtils.untilAsserted(() -> {
            var httpResponse = app.mutinyHttps(GUEST_CLIENT).get(MTLS_PATH).sendAndAwait();
            assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());
            assertEquals("Client certificate: CN=" + GUEST_CLIENT, httpResponse.bodyAsString());

            callSecuredEndpointAndExpectFailure(CLIENT);
        });
    }

    private static void callSecuredEndpointAndExpectFailure(String clientCn) {
        // this client certs are not in the server truststore, therefore they cannot be trusted
        try {
            // TODO: this says that it tests client certificates that are not in the trust store
            //  and I can see that the Mutiny client is configured with client certificates, but JDK sends
            //  "certificate_required" possibly due to https://bugs.openjdk.org/browse/JDK-8365440
            //  we should check if server receives client certificates or not; if so, behavior is correct
            app.mutinyHttps(clientCn).get(MTLS_PATH).sendAndAwait();
            // this must never happen, basically as SSL handshake must throw exception
            Assertions.fail("SSL handshake didn't fail even though certificate host is unknown");
        } catch (Exception e) {
            // failure is expected
            assertThat(e.getMessage())
                    .containsAnyOf("Received fatal alert: bad_certificate", "Received fatal alert: certificate_required")
                    .describedAs("Expected SSL handshake failure, but got: " + e);
        }
    }
}
