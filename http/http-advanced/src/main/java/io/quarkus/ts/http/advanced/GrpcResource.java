package io.quarkus.ts.http.advanced;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.example.GreeterGrpc;
import io.quarkus.example.HelloReply;
import io.quarkus.example.HelloRequest;
import io.quarkus.example.InterceptedMessageGrpc;
import io.quarkus.example.InterceptedRequest;
import io.quarkus.grpc.GrpcClient;

@Path("/grpc")
public class GrpcResource {

    @Inject
    @GrpcClient("hello")
    GreeterGrpc.GreeterBlockingStub client;

    @Inject
    @GrpcClient("hello")
    InterceptedMessageGrpc.InterceptedMessageBlockingStub interceptorsClient;

    @GET
    @Path("/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@PathParam("name") String name) {
        return client.sayHello(HelloRequest.newBuilder().setName(name).build()).getMessage();
    }

    @GET
    @Path("global/interceptors")
    public Response globalInterceptors() {
        HelloReply helloReply = interceptorsClient.showInterceptedMessage(InterceptedRequest.newBuilder().build());
        return Response.ok(helloReply.getMessage()).build();
    }

}
