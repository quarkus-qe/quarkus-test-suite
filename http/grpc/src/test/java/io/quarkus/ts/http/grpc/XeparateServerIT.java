package io.quarkus.ts.http.grpc;

import org.junit.jupiter.api.DisplayName;

import io.grpc.Channel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisplayName("SeparateServer")
//This test should be the last, or we get complains, that the channel was not shut down before closure. This is a bug in our framework.
public class XeparateServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    @QuarkusApplication(grpc = true)
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.grpc.server.use-separate-server", "true")
            .withProperty("quarkus.grpc.clients.plain.port", "${quarkus.grpc.server.port}");

    @Override
    public Channel getChannel() {
        return app.grpcChannel();
    }

    @Override
    public RestService app() {
        return app;
    }

}
