package io.quarkus.ts.http.grpc;

import static org.hamcrest.CoreMatchers.is;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.grpc.Channel;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.grpc.GreeterGrpc;
import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;
import io.quarkus.ts.grpc.StreamingGrpc;

public interface GRPCIT {

    RestService app();

    Channel getChannel();

    @Test
    default void grpcClient() {
        app().given().get("/http/grpc").then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("Hello grpc"));
    }

    @Test
    default void grpcServer() throws ExecutionException, InterruptedException {
        HelloRequest request = HelloRequest.newBuilder().setName("server").build();
        HelloReply response = GreeterGrpc.newFutureStub(getChannel()).sayHello(request).get();
        Assertions.assertEquals("Hello server", response.getMessage());
    }

    @Test
    default void serverStream() {
        HelloRequest request = HelloRequest.newBuilder().setName("ServerStream").build();
        Iterator<HelloReply> stream = StreamingGrpc.newBlockingStub(getChannel()).serverStream(request);
        AtomicInteger counter = new AtomicInteger(0);
        stream.forEachRemaining((reply) -> {
            Assertions.assertEquals("Hello ServerStream", reply.getMessage());
            counter.incrementAndGet();
        });
        Assertions.assertEquals(GrpcStreamingService.SERVER_STREAM_MESSAGES_COUNT, counter.get());
    }
}
