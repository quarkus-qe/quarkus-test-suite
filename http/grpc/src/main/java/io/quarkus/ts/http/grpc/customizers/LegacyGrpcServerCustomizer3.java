package io.quarkus.ts.http.grpc.customizers;

import jakarta.inject.Singleton;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.server.GrpcServerOptions;

@Singleton
public class LegacyGrpcServerCustomizer3 implements ServerBuilderCustomizer {

    @Override
    public void customize(GrpcServerConfiguration config, GrpcServerOptions options) {
    }

    @Override
    public int priority() {
        return 3;
    }
}
