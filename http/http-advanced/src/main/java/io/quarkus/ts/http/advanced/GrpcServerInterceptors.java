package io.quarkus.ts.http.advanced;

import static io.quarkus.ts.http.advanced.GrpcClientInterceptors.CLIENT_CLASS;
import static io.quarkus.ts.http.advanced.GrpcClientInterceptors.CLIENT_METHOD;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.quarkus.grpc.GlobalInterceptor;

class GrpcServerInterceptors {

    public static Context.Key<String> SERVER_METHOD = Context.key("server-method-target");
    public static Context.Key<String> SERVER_CLASS = Context.key("server-class-target");

    @GlobalInterceptor
    @ApplicationScoped
    static class ClassTarget extends Base {
    }

    static class MethodTarget extends Base {
    }

    static class Producer {
        @GlobalInterceptor
        @Produces
        MethodTarget methodTarget() {
            return new MethodTarget();
        }
    }

    abstract static class Base implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata,
                ServerCallHandler<ReqT, RespT> next) {
            Context ctx;
            String interceptedTarget = getClass().getName();
            // Determine where was grpc call intercepted and put client side intercepted data to Context.
            if (interceptedTarget.contains("MethodTarget")) {
                ctx = Context.current().withValue(SERVER_METHOD, metadata.get(CLIENT_METHOD));
                ctx.attach();
            } else if (interceptedTarget.contains("ClassTarget")) {
                ctx = Context.current().withValue(SERVER_CLASS, metadata.get(CLIENT_CLASS));
                ctx.attach();
            } else {
                throw new RuntimeException("Unexpected intercepted class or method by GlobalInterceptor.");
            }
            return Contexts.interceptCall(ctx, call, metadata, next);
        }
    }
}
