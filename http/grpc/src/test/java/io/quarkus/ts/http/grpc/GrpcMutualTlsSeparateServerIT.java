package io.quarkus.ts.http.grpc;

import static io.quarkus.test.services.Certificate.Format.PEM;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.OS;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.quarkus.test.security.certificate.PemClientCertificate;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.Certificate.ClientCertificate;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.mutiny.ext.web.client.WebClient;

@Tag("QUARKUS-4592")
@QuarkusScenario
public class GrpcMutualTlsSeparateServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    private static final String CERT_PREFIX = "grpc-mtls-separate-server";
    private static final String CLIENT_CN_NAME = "mtls-client-name";
    private static WebClient webClient = null;

    @QuarkusApplication(grpc = true, ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, clientCertificates = {
            @ClientCertificate(cnAttribute = CLIENT_CN_NAME)
    }, format = PEM, configureKeystore = true, configureTruststore = true, tlsConfigName = "mtls-server", configureHttpServer = true))
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.http.ssl.client-auth", "required")
            .withProperty("quarkus.profile", "mtls")
            .withProperty("grpc.client.crt", GrpcMutualTlsSeparateServerIT::getClientCert)
            .withProperty("grpc.client.ca-crt", GrpcMutualTlsSeparateServerIT::getClientCaCert)
            .withProperty("grpc.client.key", GrpcMutualTlsSeparateServerIT::getClientKey);

    public CloseableManagedChannel getChannel() {
        return app.securedGrpcChannel();
    }

    @Override
    public WebClient getWebClient() {
        if (webClient == null) {
            // HINT: we don't need to close HTTPS client as FW takes care of it
            webClient = app.mutinyHttps(CLIENT_CN_NAME);
        }
        return webClient;
    }

    private static String getClientCert() {
        return addEscapes(getClientCertificate().certPath());
    }

    private static String getClientCaCert() {
        return addEscapes(getClientCertificate().truststorePath());
    }

    private static String getClientKey() {
        return addEscapes(getClientCertificate().keyPath());
    }

    private static CertificateBuilder getCertificateBuilder() {
        return app.getPropertyFromContext(CertificateBuilder.INSTANCE_KEY);
    }

    private static PemClientCertificate getClientCertificate() {
        return (PemClientCertificate) getCertificateBuilder().findCertificateByPrefix(CERT_PREFIX)
                .getClientCertificateByCn(CLIENT_CN_NAME);
    }

    static String addEscapes(String path) {
        if (OS.WINDOWS.isCurrentOs()) {
            // TODO: move this to the FW
            // back-slashes have special meaning in Cygwin etc.
            return path.replace("\\", "\\\\");
        }
        return path;
    }
}
