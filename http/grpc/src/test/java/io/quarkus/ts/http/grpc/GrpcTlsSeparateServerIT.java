package io.quarkus.ts.http.grpc;

import static io.quarkus.test.services.Certificate.Format.PEM;
import static io.quarkus.ts.http.grpc.GrpcMutualTlsSeparateServerIT.addEscapes;

import org.junit.jupiter.api.AfterAll;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.Certificate.PemCertificate;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.mutiny.ext.web.client.WebClient;

@QuarkusScenario
public class GrpcTlsSeparateServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    private static final String CERT_PREFIX = "grpc-tls-separate-server";
    private static WebClient webClient = null;

    @QuarkusApplication(grpc = true, ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, format = PEM, configureKeystore = true, configureTruststore = true))
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.profile", "ssl")
            .withProperty("grpc.client.ca-cert", GrpcTlsSeparateServerIT::getClientCaCert)
            .withProperty("grpc.server.cert", GrpcTlsSeparateServerIT::getServerCert)
            .withProperty("grpc.server.key", GrpcTlsSeparateServerIT::getServerKey);

    public CloseableManagedChannel getChannel() {
        return app.securedGrpcChannel();
    }

    @Override
    public WebClient getWebClient() {
        if (webClient == null) {
            webClient = app.mutiny();
        }
        return webClient;
    }

    @AfterAll
    static void afterAll() {
        if (webClient != null) {
            webClient.close();
        }
    }

    private static String getClientCaCert() {
        return addEscapes(getPemCertificate().truststorePath());
    }

    private static String getServerCert() {
        return addEscapes(getPemCertificate().certPath());
    }

    private static String getServerKey() {
        return addEscapes(getPemCertificate().keyPath());
    }

    private static PemCertificate getPemCertificate() {
        return (PemCertificate) getCertificateBuilder().findCertificateByPrefix(CERT_PREFIX);
    }

    private static CertificateBuilder getCertificateBuilder() {
        return app.getPropertyFromContext(CertificateBuilder.INSTANCE_KEY);
    }
}
