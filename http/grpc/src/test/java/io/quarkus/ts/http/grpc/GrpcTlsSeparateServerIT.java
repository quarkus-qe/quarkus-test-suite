package io.quarkus.ts.http.grpc;

import static io.quarkus.test.services.Certificate.Format.PEM;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.Certificate.PemCertificate;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class GrpcTlsSeparateServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    private static final String CERT_PREFIX = "grpc-tls-separate-server";

    @QuarkusApplication(grpc = true, ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, format = PEM, configureKeystore = true, configureTruststore = true))
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.profile", "ssl")
            .withProperty("grpc.client.ca-cert", CertificateBuilder.INSTANCE_KEY, GrpcTlsSeparateServerIT::getClientCaCert)
            .withProperty("grpc.server.cert", CertificateBuilder.INSTANCE_KEY, GrpcTlsSeparateServerIT::getServerCert)
            .withProperty("grpc.server.key", CertificateBuilder.INSTANCE_KEY, GrpcTlsSeparateServerIT::getServerKey);

    public CloseableManagedChannel getChannel() {
        return app.securedGrpcChannel();
    }

    @Override
    public RestService app() {
        return app;
    }

    @Override
    public RequestSpecification given() {
        return app().relaxedHttps().given();
    }

    private static String getClientCaCert(CertificateBuilder certificateBuilder) {
        return getPemCertificate(certificateBuilder).truststorePath();
    }

    private static String getServerCert(CertificateBuilder certificateBuilder) {
        return getPemCertificate(certificateBuilder).certPath();
    }

    private static String getServerKey(CertificateBuilder certificateBuilder) {
        return getPemCertificate(certificateBuilder).keyPath();
    }

    private static PemCertificate getPemCertificate(CertificateBuilder certificateBuilder) {
        return (PemCertificate) certificateBuilder.findCertificateByPrefix(CERT_PREFIX);
    }
}
