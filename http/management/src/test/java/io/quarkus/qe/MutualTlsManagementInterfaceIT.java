package io.quarkus.qe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusScenario
public class MutualTlsManagementInterfaceIT {

    private static final String CERT_PREFIX = "qe-test";
    private static final String CLIENT_CN_1 = "client-cn-1";
    private static final String CLIENT_CN_2 = "client-cn-2";
    private static final String TLS_CONFIG_NAME = "mtls-management";

    @QuarkusApplication(certificates = @Certificate(prefix = CERT_PREFIX, clientCertificates = {
            @ClientCertificate(cnAttribute = CLIENT_CN_1),
            @ClientCertificate(cnAttribute = CLIENT_CN_2, unknownToServer = true)
    }, configureKeystore = true, configureTruststore = true, tlsConfigName = TLS_CONFIG_NAME, configureManagementInterface = true))
    static final RestService app = new RestService()
            .withProperty("quarkus.management.ssl.client-auth", "required")
            .withProperty("quarkus.tls." + TLS_CONFIG_NAME + ".reload-period", "2s");

    @Order(1)
    @Test
    public void testMutualTlsForManagementInterface() {
        // test health probe
        var client1 = app.mutinyHttps(CLIENT_CN_1);
        var httpResponse = client1.get("/q/health").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());

        // test custom management endpoint
        httpResponse = client1.get("/management-ping").sendAndAwait();
        assertEquals("pong", httpResponse.bodyAsString());
        assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());

        callManagementPingRouteAndExpectFailure(CLIENT_CN_2);
    }

    @Order(2)
    @Test
    public void testCertificateReloading() {
        app
                .<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .regenerateCertificate(CERT_PREFIX, certRequest -> {
                    // regenerate client certificates
                    // make the first client invalid
                    var clientOneCert = new ClientCertificateRequest(CLIENT_CN_1, true);
                    // make the second client valid
                    var clientTwoCert = new ClientCertificateRequest(CLIENT_CN_2, false);
                    certRequest.withClientRequests(clientOneCert, clientTwoCert);
                });

        // now we expect opposite from what we tested in step one: client 1 must fail and client 2 must succeed
        AwaitilityUtils.untilAsserted(() -> {
            var httpResponse = app.mutinyHttps(CLIENT_CN_2).get("/management-ping").sendAndAwait();
            assertEquals("pong", httpResponse.bodyAsString());
            assertEquals(HttpStatus.SC_OK, httpResponse.statusCode());

            callManagementPingRouteAndExpectFailure(CLIENT_CN_1);
        });
    }

    private static void callManagementPingRouteAndExpectFailure(String clientCn) {
        // this client certs are not in the server truststore, therefore they cannot be trusted
        try {
            app.mutinyHttps(clientCn).get("/management-ping").sendAndAwait();
            // this must never happen, basically as SSL handshake must throw exception
            Assertions.fail("SSL handshake didn't fail even though certificate host is unknown");
        } catch (Exception e) {
            // failure is expected
            assertTrue(e.getMessage().contains("Received fatal alert: bad_certificate"),
                    "Expected failure over bad certificate, but got: " + e.getMessage());
        }
    }
}
