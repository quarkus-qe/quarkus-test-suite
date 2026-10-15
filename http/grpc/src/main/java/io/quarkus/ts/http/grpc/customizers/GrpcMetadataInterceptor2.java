package io.quarkus.ts.http.grpc.customizers;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import io.quarkus.grpc.GlobalInterceptor;

@GlobalInterceptor
@Singleton
@Priority(2)
public class GrpcMetadataInterceptor2 extends GrpcMetadataInterceptor {

    GrpcMetadataInterceptor2(GrpcServerCustomizerHelper helper) {
        super(LegacyGrpcServerCustomizer2.class, helper);
    }
}
