package io.quarkus.ts.http.grpc;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;

@OpenShiftScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-framework/issues/1052+1053")
public class OpenShiftGRPCIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    @Override
    public RestService app() {
        return null;
    }

    @Override
    public CloseableManagedChannel getChannel() {
        return null;
    }
}
