package io.quarkus.ts.http.grpc;

import org.junit.jupiter.api.AfterAll;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.mutiny.ext.web.client.WebClient;

@QuarkusScenario
public class SameServerIT implements GRPCIT, ReflectionHttpIT, StreamingHttpIT {

    private static WebClient webClient = null;

    @QuarkusApplication(grpc = true)
    static final GrpcService app = new GrpcService();

    @Override
    public CloseableManagedChannel getChannel() {
        return app.grpcChannel();
    }

    @Override
    public WebClient getWebClient() {
        if (webClient == null) {
            webClient = app.mutiny();
        }
        return webClient;
    }

    @AfterAll
    static void afterAll() {
        if (webClient != null) {
            webClient.close();
        }
    }
}
