package io.quarkus.ts.http.grpc;

import static io.quarkus.test.services.Certificate.Format.PEM;
import static io.quarkus.ts.http.grpc.GrpcMutualTlsSameServerIT.addEscapes;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.Certificate.PemCertificate;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.mutiny.ext.web.client.WebClient;

@QuarkusScenario
public class GrpcTlsSameServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT, GrpcSameServerCustomizationIT {

    private static final String CERT_PREFIX = "grpc-tls-same-server";
    private static WebClient webClient = null;

    @QuarkusApplication(grpc = true, ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, format = PEM, configureKeystore = true, configureTruststore = true))
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.profile", "ssl")
            .withProperty("grpc.client.ca-cert", GrpcTlsSameServerIT::getClientCaCert)
            .withProperty("grpc.server.cert", GrpcTlsSameServerIT::getServerCert)
            .withProperty("grpc.server.key", GrpcTlsSameServerIT::getServerKey);

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
