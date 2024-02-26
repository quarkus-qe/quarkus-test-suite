package io.quarkus.ts.http.grpc;

import java.time.Duration;
import java.util.stream.Collectors;

import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;
import io.quarkus.ts.grpc.Streaming;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@io.quarkus.grpc.GrpcService
public class GrpcStreamingService implements Streaming {

    public static final int SERVER_STREAM_MESSAGES_COUNT = 5;

    @Override
    public Multi<HelloReply> serverStream(HelloRequest request) {
        return Multi.createFrom().ticks().every(Duration.ofMillis(1000)) // Produce message every second
                .select().first(SERVER_STREAM_MESSAGES_COUNT)
                .map(l -> HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
    }

    @Override
    public Uni<HelloReply> clientStream(Multi<HelloRequest> request) {
        return request
                .collect()
                .with(Collectors.counting())
                .map(total -> HelloReply.newBuilder().setMessage("Total names submitted: " + total).build());
    }

    @Override
    public Multi<HelloReply> bidirectionalStream(Multi<HelloRequest> request) {
        return request
                .map(HelloRequest::getName)
                .onItem().scan(() -> "", (subtotal, name) -> subtotal + name + ";") // Append name and send response
                .onItem().transform(names -> HelloReply.newBuilder().setMessage("Hello: " + names).build());
    }
}
