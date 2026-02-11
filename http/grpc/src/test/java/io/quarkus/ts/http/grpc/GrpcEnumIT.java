package io.quarkus.ts.http.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.grpc.demo.DemoEnum;
import io.quarkus.ts.grpc.demo.DemoGrpc;
import io.quarkus.ts.grpc.demo.EnumTriggerReply;
import io.quarkus.ts.grpc.demo.EnumTriggerRequest;

@Tag("https://github.com/quarkusio/quarkus/issues/47936")
@QuarkusScenario
public class GrpcEnumIT {

    private static final Logger LOG = Logger.getLogger(GrpcEnumIT.class);

    @QuarkusApplication(grpc = true)
    static final GrpcService app = new GrpcService();

    @Test
    public void testClientSideEnumToStringInNativeMode() {
        EnumTriggerRequest request = EnumTriggerRequest.newBuilder()
                .setName("test")
                .setEnum(DemoEnum.B)
                .build();
        String requestString = request.toString();
        LOG.info("Request as string: " + requestString);
        assertTrue(requestString.contains("name: \"test\""));
        assertTrue(requestString.contains("enum: B"));

    }

    @Test
    @DisabledOnNative(reason = "https://github.com/quarkusio/quarkus/issues/52436")
    public void testServerSideEnumLoggingInNativeMode() {
        try (var channel = app.grpcChannel()) {
            EnumTriggerRequest request = EnumTriggerRequest.newBuilder()
                    .setName("logging-test")
                    .setEnum(DemoEnum.B)
                    .build();

            EnumTriggerReply response = DemoGrpc.newBlockingStub(channel)
                    .triggerEnumError(request);

            assertEquals("logging-test", response.getName());
            assertEquals(DemoEnum.B, response.getEnum());
        }
    }
}
