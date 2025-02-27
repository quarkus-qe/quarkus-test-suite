package io.quarkus.ts.http.grpc;

import static io.quarkus.test.services.Certificate.Format.ENCRYPTED_PEM;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Certificate;
import io.quarkus.test.services.Certificate.ClientCertificate;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.grpc.GreeterGrpc;
import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;

@QuarkusScenario
public class GrpcMtlsEncryptedPemTlsRegistryIT {

    private static final String CERT_PREFIX = "grpc-mtls-separate-server";
    private static final String CLIENT_CN_NAME = "mtls-client-name";
    @QuarkusApplication(grpc = true, ssl = true, certificates = @Certificate(prefix = CERT_PREFIX, clientCertificates = {
            @ClientCertificate(cnAttribute = CLIENT_CN_NAME) }, format = ENCRYPTED_PEM, configureKeystore = true, configureTruststore = true, tlsConfigName = "mtls-server", configureHttpServer = true))
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.grpc.server.use-separate-server", "false")
            .withProperty("quarkus.http.insecure-requests", "disabled")
            .withProperty("quarkus.http.ssl.client-auth", "request")
            .withProperty("quarkus.http.auth.permission.perm-1.policy", "authenticated")
            .withProperty("quarkus.http.auth.permission.perm-1.paths", "*")
            .withProperty("quarkus.http.auth.permission.perm-1.auth-mechanism", "X509");

    @Test
    public void testMutualTlsCommunicationWithHelloService() {
        try (var channel = app.securedGrpcChannel()) {
            HelloRequest request = HelloRequest.newBuilder().setName(CLIENT_CN_NAME).build();
            HelloReply response = GreeterGrpc.newBlockingStub(channel).sayHello(request);
            assertEquals("Hello " + CLIENT_CN_NAME, response.getMessage());
        }
    }

}
