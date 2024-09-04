package io.quarkus.ts.http.grpc;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SeparateServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    @QuarkusApplication(grpc = true)
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.grpc.server.use-separate-server", "true")
            .withProperty("quarkus.grpc.clients.plain.port", "${quarkus.grpc.server.port}");

    @Override
    public CloseableManagedChannel getChannel() {
        return app.grpcChannel();
    }

    @Override
    public RestService app() {
        return app;
    }

}
