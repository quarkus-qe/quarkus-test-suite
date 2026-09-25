package io.quarkus.ts.http.grpc.customizers;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.quarkus.grpc.GlobalInterceptor;

@GlobalInterceptor
@Singleton
@Priority(0)
public class MetadataPropagatingInterceptor implements ServerInterceptor {

    private final GrpcServerCustomizerHelper helper;

    MetadataPropagatingInterceptor(GrpcServerCustomizerHelper helper) {
        this.helper = helper;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata,
            ServerCallHandler<ReqT, RespT> next) {
        var ctx = helper.putMetadataInvocationsToContext(metadata);
        ctx.attach();
        return Contexts.interceptCall(ctx, call, metadata, next);
    }
}
