package io.quarkus.ts.http.grpc.customizers;

import jakarta.inject.Singleton;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.grpc.server.GrpcServerOptions;

@Singleton
public class VertxGrpcServerCustomizer3 implements ServerBuilderCustomizer<VertxServerBuilder> {

    @Override
    public void customize(GrpcServerConfiguration config, GrpcServerOptions options) {
        options.setMaxMessageSize(90);
    }

    @Override
    public int priority() {
        return 3;
    }
}
