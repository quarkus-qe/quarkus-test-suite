package io.quarkus.ts.http.grpc.customizers;

import jakarta.enterprise.context.Dependent;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.server.GrpcServerOptions;

@Dependent
public class LegacyGrpcServerCustomizer2 implements ServerBuilderCustomizer {

    @Override
    public void customize(GrpcServerConfiguration config, GrpcServerOptions options) {
    }

    @Override
    public int priority() {
        return 2;
    }
}
