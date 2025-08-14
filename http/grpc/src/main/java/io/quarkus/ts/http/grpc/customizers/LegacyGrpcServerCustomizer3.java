package io.quarkus.ts.http.grpc.customizers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.VertxServerBuilder;

@Singleton
public class LegacyGrpcServerCustomizer3 implements ServerBuilderCustomizer<VertxServerBuilder> {

    @Inject
    GrpcServerCustomizerHelper helper;

    @Override
    public void customize(GrpcServerConfiguration config, VertxServerBuilder builder) {
        builder.intercept(new GrpcMetadataInterceptor(this.getClass(), helper));
    }

    @Override
    public int priority() {
        return 3;
    }
}
