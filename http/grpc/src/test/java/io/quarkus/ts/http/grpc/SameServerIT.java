package io.quarkus.ts.http.grpc;

import org.junit.jupiter.api.AfterAll;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SameServerIT implements GRPCIT, StreamingHttpIT {

    @QuarkusApplication(grpc = true)
    static final GrpcService app = new GrpcService();
    private static ManagedChannel channel;

    @Override
    public Channel getChannel() {
        if (channel == null) {
            channel = ManagedChannelBuilder.forAddress(
                    app.getURI().getHost(),
                    app.getURI().getPort())
                    .usePlaintext()
                    .build();
        }
        return channel;
    }

    @Override
    public RestService app() {
        return app;
    }

    @AfterAll
    static void afterAll() {
        channel.shutdown();
    }
}
