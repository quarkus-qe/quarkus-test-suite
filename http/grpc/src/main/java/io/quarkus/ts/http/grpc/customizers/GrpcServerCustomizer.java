package io.quarkus.ts.http.grpc.customizers;

import jakarta.enterprise.context.ApplicationScoped;

import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.quarkus.grpc.api.ServerBuilderCustomizer;
import io.quarkus.grpc.runtime.config.GrpcServerConfiguration;
import io.quarkus.ts.grpc.metadata.MetadataReply;
import io.quarkus.ts.grpc.metadata.MetadataRequest;
import io.quarkus.ts.grpc.metadata.MutinyMetadataGrpc;
import io.smallrye.mutiny.Uni;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.grpc.server.GrpcServerOptions;

@ApplicationScoped
public class GrpcServerCustomizer implements ServerBuilderCustomizer<VertxServerBuilder> {

    private final GrpcServerCustomizerHelper helper;

    GrpcServerCustomizer(GrpcServerCustomizerHelper helper) {
        this.helper = helper;
    }

    @Override
    public void customize(GrpcServerConfiguration config, VertxServerBuilder builder) {
        builder.addService(new MetadataGrpcService());
        builder.intercept(new MetadataPropagatingInterceptor());
    }

    @Override
    public void customize(GrpcServerConfiguration config, GrpcServerOptions options) {
        options.setMaxMessageSize(1);
    }

    private final class MetadataPropagatingInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata,
                ServerCallHandler<ReqT, RespT> next) {
            var ctx = helper.putMetadataInvocationsToContext(metadata);
            ctx.attach();
            return Contexts.interceptCall(ctx, call, metadata, next);
        }
    }

    private final class MetadataGrpcService extends MutinyMetadataGrpc.MetadataImplBase {

        @Override
        public Uni<MetadataReply> getMetadata(MetadataRequest request) {
            InterceptorInvocations invocations = helper.getInvocationsFromContext();
            MetadataReply response = MetadataReply.newBuilder()
                    .setInterceptedFirst(invocations.interceptedFirst())
                    .setInterceptedSecond(invocations.interceptedSecond())
                    .setInterceptedThird(invocations.interceptedThird())
                    .setRequestMessage(request.getMessage())
                    .build();
            return Uni.createFrom().item(response);
        }
    }
}
