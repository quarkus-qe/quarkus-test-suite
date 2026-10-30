package io.quarkus.ts.http.grpc.customizers;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.server.GrpcServerOptions;

@ApplicationScoped
public class LegacyGrpcServerCustomizer implements ServerBuilderCustomizer {

    @Override
    public void customize(GrpcServerConfiguration config, GrpcServerOptions options) {
    }

    @Override
    public int priority() {
        return 1;
    }
}
