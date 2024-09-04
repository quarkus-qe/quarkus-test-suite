package io.quarkus.ts.http.grpc;

import java.io.IOException;
import java.io.InputStream;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.TlsChannelCredentials;
import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class TLSIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    @QuarkusApplication(grpc = true, ssl = true)
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.profile", "ssl");

    public CloseableManagedChannel getChannel() {
        try (InputStream caCertificate = app.getClass().getClassLoader().getResourceAsStream("tls/ca.pem")) {
            ChannelCredentials credentials = TlsChannelCredentials.newBuilder()
                    .trustManager(caCertificate)
                    .build();
            var channel = Grpc.newChannelBuilderForAddress(app().getURI(Protocol.GRPC).getHost(),
                    app().getURI(Protocol.HTTPS).getPort(), credentials)
                    .build();
            return new CloseableManagedChannel(channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RestService app() {
        return app;
    }

    @Override
    public RequestSpecification given() {
        return app().relaxedHttps().given();
    }

}
