package io.quarkus.ts.http.grpc;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.ts.grpc.Greeter;
import io.quarkus.ts.grpc.HelloRequest;
import io.smallrye.mutiny.Uni;

@Path("/http")
public class HttpResource {

    @GrpcClient
    Greeter plain;

    @GET
    @Path("/{name}")
    public Uni<String> hello(String name) {
        return plain.sayHello(HelloRequest.newBuilder().setName(name).build())
                .onItem().transform(helloReply -> helloReply.getMessage());
    }
}
