package io.quarkus.ts.http.grpc.customizers;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

final class GrpcMetadataInterceptor implements ServerInterceptor {

    private final String customizerName;
    private final GrpcServerCustomizerHelper helper;

    GrpcMetadataInterceptor(Class<?> customizerClass, GrpcServerCustomizerHelper helper) {
        this.customizerName = customizerClass.getName();
        this.helper = helper;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        helper.recordInterceptorInvocation(metadata, customizerName);
        return serverCallHandler.startCall(serverCall, metadata);
    }
}
