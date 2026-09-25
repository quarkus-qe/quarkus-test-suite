package io.quarkus.ts.http.grpc.customizers;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.server.GrpcServerOptions;

@ApplicationScoped
public class GrpcServerCustomizer implements ServerBuilderCustomizer {

    @Override
    public void customize(GrpcServerConfiguration config, GrpcServerOptions options) {
        options.setMaxMessageSize(1);
    }
}
