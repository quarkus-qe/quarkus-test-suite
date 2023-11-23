package io.quarkus.ts.http.advanced;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.quarkus.grpc.GlobalInterceptor;

class GrpcClientInterceptors {
    public static Metadata.Key<String> CLIENT_METHOD = Metadata.Key.of("client-method-target", ASCII_STRING_MARSHALLER);
    public static Metadata.Key<String> CLIENT_CLASS = Metadata.Key.of("client-class-target", ASCII_STRING_MARSHALLER);

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

    abstract static class Base implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions options,
                Channel next) {
            String interceptedTarget = getClass().getName();
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, options)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    if (interceptedTarget.contains("MethodTarget")) {
                        headers.put(CLIENT_METHOD, interceptedTarget);
                    } else if (interceptedTarget.contains("ClassTarget")) {
                        headers.put(CLIENT_CLASS, interceptedTarget);
                    }
                    super.start(responseListener, headers);
                }
            };
        }
    }
}
