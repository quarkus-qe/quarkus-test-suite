package io.quarkus.ts.http.grpc.customizers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.vertx.grpc.VertxServerBuilder;

@ApplicationScoped
public class LegacyGrpcServerCustomizer implements ServerBuilderCustomizer<VertxServerBuilder> {

    @Inject
    GrpcServerCustomizerHelper helper;

    @Override
    public void customize(GrpcServerConfiguration config, VertxServerBuilder builder) {
        builder.intercept(new GrpcMetadataInterceptor(this.getClass(), helper));
    }

    @Override
    public int priority() {
        return 1;
    }
}
