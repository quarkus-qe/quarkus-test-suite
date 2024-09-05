package io.quarkus.ts.http.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.CloseableManagedChannel;
import io.quarkus.ts.grpc.GreeterGrpc;
import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;
import io.quarkus.ts.grpc.StreamingGrpc;
import io.vertx.mutiny.ext.web.client.WebClient;

public interface GRPCIT {

    CloseableManagedChannel getChannel();

    WebClient getWebClient();

    @Test
    default void grpcClient() {
        var response = getWebClient().get("/http/grpc").sendAndAwait();
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertTrue(response.bodyAsString().startsWith("Hello grpc"));
    }

    @Test
    default void grpcServer() throws ExecutionException, InterruptedException {
        try (var channel = getChannel()) {
            HelloRequest request = HelloRequest.newBuilder().setName("server").build();
            HelloReply response = GreeterGrpc.newFutureStub(channel).sayHello(request).get();
            assertEquals("Hello server", response.getMessage());
        }
    }

    @Test
    default void serverStream() {
        try (var channel = getChannel()) {
            HelloRequest request = HelloRequest.newBuilder().setName("ServerStream").build();
            Iterator<HelloReply> stream = StreamingGrpc.newBlockingStub(channel).serverStream(request);
            AtomicInteger counter = new AtomicInteger(0);
            stream.forEachRemaining((reply) -> {
                assertEquals("Hello ServerStream", reply.getMessage());
                counter.incrementAndGet();
            });
            assertEquals(GrpcStreamingService.SERVER_STREAM_MESSAGES_COUNT, counter.get());
        }
    }
}
