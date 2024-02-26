package io.quarkus.ts.http.grpc;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.grpc.reflection.v1.MutinyServerReflectionGrpc;
import io.grpc.reflection.v1.ServerReflectionRequest;
import io.grpc.reflection.v1.ServerReflectionResponse;
import io.grpc.reflection.v1.ServiceResponse;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/http/reflection")
public class HttpReflectionResource {

    @Inject
    @GrpcClient("reflection-service")
    MutinyServerReflectionGrpc.MutinyServerReflectionStub reflection;

    @GET
    @Path("/service/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<GrpcReflectionResponse> reflectionServiceInfo() {
        ServerReflectionRequest request = ServerReflectionRequest.newBuilder().setHost("localhost")
                .setListServices("").build();
        Uni<ServerReflectionResponse> serverReflectionResponse = invoke(request);

        Uni<GrpcReflectionResponse> grpcReflectionResponse = serverReflectionResponse.map(response -> {
            int serviceCount = response.getListServicesResponse().getServiceCount();
            List<String> reflectionServiceList = getReflectionServiceList(response);
            return new GrpcReflectionResponse(serviceCount, reflectionServiceList);
        });

        return grpcReflectionResponse;
    }

    @GET
    @Path("/descriptor/{protoFilename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<byte[]> reflectionFileDescriptor(@PathParam("protoFilename") String protoFilename) {
        ServerReflectionRequest request = ServerReflectionRequest.newBuilder()
                .setHost("localhost").setFileByFilename(protoFilename + ".proto").build();

        Uni<ServerReflectionResponse> response = invoke(request);
        return response.map(item -> item.getFileDescriptorResponse().toByteArray());
    }

    private Uni<ServerReflectionResponse> invoke(ServerReflectionRequest request) {
        return reflection.serverReflectionInfo(Multi.createFrom().item(request))
                .collect().first();
    }

    private List<String> getReflectionServiceList(ServerReflectionResponse response) {
        List<ServiceResponse> serviceList = response.getListServicesResponse().getServiceList();
        List<String> serviceNames = new ArrayList<>();
        for (var service : serviceList) {
            serviceNames.add(service.getName());
        }
        return serviceNames;
    }
}
