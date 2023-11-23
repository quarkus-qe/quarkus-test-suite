package io.quarkus.ts.http.advanced;

import io.grpc.stub.StreamObserver;
import io.quarkus.example.HelloReply;
import io.quarkus.example.InterceptedMessageGrpc;
import io.quarkus.example.InterceptedRequest;
import io.quarkus.grpc.GrpcService;

@GrpcService
public class GrpcInterceptorsService extends InterceptedMessageGrpc.InterceptedMessageImplBase {

    @Override
    public void showInterceptedMessage(InterceptedRequest request, StreamObserver<HelloReply> responseObserver) {
        String serverMethod = GrpcServerInterceptors.SERVER_METHOD.get();
        String serverClass = GrpcServerInterceptors.SERVER_CLASS.get();
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Intercepted client side method passed by server method interceptor is: " + serverMethod
                        + "\nIntercepted client side class passed by server class interceptor is: " + serverClass)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
