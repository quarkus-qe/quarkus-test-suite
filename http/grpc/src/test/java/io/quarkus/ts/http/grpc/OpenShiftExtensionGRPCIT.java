package io.quarkus.ts.http.grpc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import io.grpc.Channel;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;

@Tag("use-quarkus-openshift-extension")
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingOpenShiftExtension)
@Disabled("https://github.com/quarkus-qe/quarkus-test-framework/issues/1052+1053")
public class OpenShiftExtensionGRPCIT implements GRPCIT, StreamingHttpIT {

    @Override
    public RestService app() {
        return null;
    }

    @Override
    public Channel getChannel() {
        return null;
    }
}
