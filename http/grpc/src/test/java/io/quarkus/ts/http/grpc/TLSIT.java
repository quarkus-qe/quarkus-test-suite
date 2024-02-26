package io.quarkus.ts.http.grpc;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterAll;

import io.grpc.Channel;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class TLSIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    private static ManagedChannel channel;
    @QuarkusApplication(grpc = true, ssl = true)
    // See https://github.com/quarkusio/quarkus/issues/38965 to learn, why we use these parameters
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.grpc.clients.plain.port", "${quarkus.http.ssl-port}")
            //            .withProperty("quarkus.grpc.clients.streaming.port", "${quarkus.http.ssl-port}")
            .withProperty("quarkus.http.ssl.certificate.files", "tls/server.pem")
            .withProperty("quarkus.http.ssl.certificate.key-files", "tls/server.key")
            .withProperty("quarkus.grpc.server.ssl.certificate", "tls/server.pem")
            .withProperty("quarkus.grpc.server.ssl.key", "tls/server.key")
            .withProperty("quarkus.grpc.clients.plain.ssl.trust-store", "tls/ca.pem");

    public Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        try (InputStream caCertificate = app.getClass().getClassLoader().getResourceAsStream("tls/ca.pem")) {
            ChannelCredentials credentials = TlsChannelCredentials.newBuilder()
                    .trustManager(caCertificate)
                    .build();
            channel = Grpc.newChannelBuilderForAddress(app().getURI(Protocol.GRPC).getHost(),
                    app().getURI(Protocol.HTTPS).getPort(), credentials)
                    .build();
            return channel;
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

    @AfterAll
    static void afterAll() {
        channel.shutdown();
    }
}
