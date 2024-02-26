package io.quarkus.ts.http.grpc;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.ts.grpc.HelloReply;
import io.quarkus.ts.grpc.HelloRequest;
import io.quarkus.ts.grpc.MutinyStreamingGrpc;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/http/streaming")
public class HttpStreamingResource {

    @GrpcClient
    MutinyStreamingGrpc.MutinyStreamingStub streaming;

    @GET
    @Path("/server/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<String> server(String name) {
        return streaming.serverStream(createRequest(name))
                .onItem().transform(HelloReply::getMessage);
    }

    private static HelloRequest createRequest(String name) {
        return HelloRequest.newBuilder()
                .setName(name).build();
    }

    @POST
    @Path("/client")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> client(List<String> names) {
        return streaming.clientStream(Multi.createFrom()
                .iterable(names)
                .map(HttpStreamingResource::createRequest))
                .map(HelloReply::getMessage);
    }

    @POST
    @Path("/bi")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Multi<String> biderectional(List<String> names) {
        return streaming.bidirectionalStream(Multi.createFrom()
                .iterable(names)
                .map(HttpStreamingResource::createRequest))
                .map(HelloReply::getMessage);
    }
}
