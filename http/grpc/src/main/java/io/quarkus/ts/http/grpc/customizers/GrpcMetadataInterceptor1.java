package io.quarkus.ts.http.grpc.customizers;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;

import io.quarkus.grpc.GlobalInterceptor;

@GlobalInterceptor
@Singleton
@Priority(1)
public class GrpcMetadataInterceptor1 extends GrpcMetadataInterceptor {

    GrpcMetadataInterceptor1(GrpcServerCustomizerHelper helper) {
        super(LegacyGrpcServerCustomizer.class, helper);
    }
}
