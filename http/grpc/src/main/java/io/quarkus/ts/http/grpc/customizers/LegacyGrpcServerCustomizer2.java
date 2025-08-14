package io.quarkus.ts.http.grpc.customizers;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.VertxServerBuilder;

@Dependent
public class LegacyGrpcServerCustomizer2 implements ServerBuilderCustomizer<VertxServerBuilder> {

    @Inject
    GrpcServerCustomizerHelper helper;

    @Override
    public void customize(GrpcServerConfiguration config, VertxServerBuilder builder) {
        builder.intercept(new GrpcMetadataInterceptor(this.getClass(), helper));
    }

    @Override
    public int priority() {
        return 2;
    }
}
