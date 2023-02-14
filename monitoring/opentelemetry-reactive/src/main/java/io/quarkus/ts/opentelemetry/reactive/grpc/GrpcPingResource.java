package io.quarkus.ts.opentelemetry.reactive.grpc;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.example.PongRequest;
import io.quarkus.example.PongServiceGrpc;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.ts.opentelemetry.reactive.traceable.TraceableResource;

@Path("/grpc-ping")
public class GrpcPingResource extends TraceableResource {

    @Inject
    @GrpcClient("pong")
    PongServiceGrpc.PongServiceBlockingStub pongClient;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPing() {
        recordTraceId();

        return "ping " + pongClient.sayPong(PongRequest.newBuilder().build()).getMessage();
    }
}
