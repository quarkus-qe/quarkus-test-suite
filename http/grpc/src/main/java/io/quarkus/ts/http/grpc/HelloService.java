package io.quarkus.ts.http.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.ts.grpc.Greeter;
import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;
import io.smallrye.mutiny.Uni;

@GrpcService
public class HelloService implements Greeter {

    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
        return Uni.createFrom()
                .item(() -> HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
    }
}
