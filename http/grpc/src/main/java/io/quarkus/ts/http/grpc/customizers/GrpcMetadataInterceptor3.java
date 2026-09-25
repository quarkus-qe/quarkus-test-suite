package io.quarkus.ts.http.grpc.customizers;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import io.quarkus.grpc.GlobalInterceptor;

@GlobalInterceptor
@Singleton
@Priority(3)
public class GrpcMetadataInterceptor3 extends GrpcMetadataInterceptor {

    GrpcMetadataInterceptor3(GrpcServerCustomizerHelper helper) {
        super(LegacyGrpcServerCustomizer3.class, helper);
    }
}
