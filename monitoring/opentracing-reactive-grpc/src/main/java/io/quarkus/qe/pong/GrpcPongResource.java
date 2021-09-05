package io.quarkus.qe.pong;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

import io.quarkus.example.LastTraceIdRequest;
import io.quarkus.example.PongServiceGrpc;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.qe.traceable.TraceableResource;

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
