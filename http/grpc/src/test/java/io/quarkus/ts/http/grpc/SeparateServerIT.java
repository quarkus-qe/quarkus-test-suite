package io.quarkus.ts.http.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.grpc.metadata.MetadataGrpc;
import io.quarkus.ts.grpc.metadata.MetadataReply;
import io.quarkus.ts.grpc.metadata.MetadataRequest;
import io.quarkus.ts.http.grpc.customizers.LegacyGrpcServerCustomizer;
import io.quarkus.ts.http.grpc.customizers.LegacyGrpcServerCustomizer2;
import io.quarkus.ts.http.grpc.customizers.LegacyGrpcServerCustomizer3;
import io.vertx.mutiny.ext.web.client.WebClient;

@QuarkusScenario
public class SeparateServerIT implements GRPCIT, StreamingHttpIT, ReflectionHttpIT {

    private static WebClient webClient = null;

    @QuarkusApplication(grpc = true)
    static final GrpcService app = (GrpcService) new GrpcService()
            .withProperty("quarkus.grpc.server.use-separate-server", "true")
            .withProperty("quarkus.grpc.clients.plain.port", "${quarkus.grpc.server.port}");

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

    @Test
    void testServerCustomizations() throws ExecutionException, InterruptedException {
        try (var channel = getChannel()) {
            MetadataRequest request = MetadataRequest.newBuilder().setMessage("Hey").build();
            MetadataReply response = MetadataGrpc.newFutureStub(channel).getMetadata(request).get();
            assertEquals(request.getMessage(), response.getRequestMessage());
            // if the first customizer adds interceptor A and the second adds interceptor B
            // then the B is invoked first and the A is invoked second and so on
            assertEquals(LegacyGrpcServerCustomizer3.class.getName(), response.getInterceptedFirst());
            assertEquals(LegacyGrpcServerCustomizer2.class.getName(), response.getInterceptedSecond());
            assertEquals(LegacyGrpcServerCustomizer.class.getName(), response.getInterceptedThird());
        }
    }
}
