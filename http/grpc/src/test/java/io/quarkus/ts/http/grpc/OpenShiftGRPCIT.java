package io.quarkus.ts.http.grpc;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.vertx.mutiny.ext.web.client.WebClient;

@OpenShiftScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-framework/issues/1052+1053")
public class OpenShiftGRPCIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    @Override
    public CloseableManagedChannel getChannel() {
        return null;
    }

    @Override
    public WebClient getWebClient() {
        return null;
    }
}
