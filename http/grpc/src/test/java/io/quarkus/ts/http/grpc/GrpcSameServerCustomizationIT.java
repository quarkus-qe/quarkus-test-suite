package io.quarkus.ts.http.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;
import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.ts.grpc.GreeterGrpc;
import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;

public interface GrpcSameServerCustomizationIT {

    CloseableManagedChannel getChannel();

    @Test
    default void testServerCustomizations() throws ExecutionException, InterruptedException {
        try (var channel = getChannel()) {
            String name = "Black Lung";
            HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            HelloReply response = GreeterGrpc.newFutureStub(channel).sayHello(request).get();
            assertEquals("Hello " + name, response.getMessage());
            name = "Black Lung".repeat(10);
            request = HelloRequest.newBuilder().setName(name).build();
            try {
                GreeterGrpc.newFutureStub(channel).sayHello(request).get();
                Assertions.fail("Should have thrown an exception");
            } catch (Throwable t) {
                Throwable cause = t;
                while (cause != null) {
                    if (cause instanceof StatusRuntimeException) {
                        break;
                    }
                    cause = cause.getCause();
                }
                assertInstanceOf(StatusRuntimeException.class, cause);
                StatusRuntimeException statusRuntimeException = (StatusRuntimeException) cause;
                assertFalse(statusRuntimeException.getStatus().isOk());
                assertTrue(statusRuntimeException.getMessage().contains("RESOURCE_EXHAUSTED"),
                        () -> "Expected that the exception message will contain 'RESOURCE_EXHAUSTED', but got: "
                                + statusRuntimeException.getMessage());
            }
        }
    }

}
