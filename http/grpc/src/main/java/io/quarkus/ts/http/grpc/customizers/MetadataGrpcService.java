package io.quarkus.ts.http.grpc.customizers;

import io.quarkus.grpc.GrpcService;
import io.quarkus.ts.grpc.metadata.MetadataReply;
import io.quarkus.ts.grpc.metadata.MetadataRequest;
import io.quarkus.ts.grpc.metadata.MutinyMetadataGrpc;
import io.smallrye.mutiny.Uni;

@GrpcService
public class MetadataGrpcService extends MutinyMetadataGrpc.MetadataImplBase {

    private final GrpcServerCustomizerHelper helper;

    MetadataGrpcService(GrpcServerCustomizerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Uni<MetadataReply> getMetadata(MetadataRequest request) {
        InterceptorInvocations invocations = helper.getInvocationsFromContext();
        MetadataReply response = MetadataReply.newBuilder()
                .setInterceptedFirst(invocations.interceptedFirst())
                .setInterceptedSecond(invocations.interceptedSecond())
                .setInterceptedThird(invocations.interceptedThird())
                .setRequestMessage(request.getMessage())
                .build();
        return Uni.createFrom().item(response);
    }
}
