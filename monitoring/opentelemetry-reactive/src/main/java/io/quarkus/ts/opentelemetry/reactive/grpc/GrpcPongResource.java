package io.quarkus.ts.opentelemetry.reactive.grpc;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.example.LastTraceIdRequest;
import io.quarkus.example.PongServiceGrpc;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.ts.opentelemetry.reactive.traceable.TraceableResource;

@Path("/grpc-pong")
public class GrpcPongResource {

    @Inject
    @GrpcClient("pong")
    PongServiceGrpc.PongServiceBlockingStub pongClient;

    private static final Logger LOG = Logger.getLogger(TraceableResource.class);

    @GET
    @Path("/lastTraceId")
    @Produces(MediaType.TEXT_PLAIN)
    public String getLastTraceId() {
        String lastTraceId = pongClient.returnLastTraceId(LastTraceIdRequest.newBuilder().build()).getMessage();
        LOG.info("Recorded trace ID: " + lastTraceId);
        return lastTraceId;
    }

}
