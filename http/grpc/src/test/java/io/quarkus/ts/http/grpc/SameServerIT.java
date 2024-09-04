package io.quarkus.ts.http.grpc;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class SameServerIT implements GRPCIT, ReflectionHttpIT, StreamingHttpIT {

    @QuarkusApplication(grpc = true)
    static final GrpcService app = new GrpcService();

    @Override
    public CloseableManagedChannel getChannel() {
        return app.grpcChannel();
    }

    @Override
    public RestService app() {
        return app;
    }

}
