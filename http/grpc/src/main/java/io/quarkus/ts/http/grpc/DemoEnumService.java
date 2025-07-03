package io.quarkus.ts.http.grpc;

import org.jboss.logging.Logger;

import io.quarkus.grpc.GrpcService;
import io.quarkus.ts.grpc.demo.Demo;
import io.quarkus.ts.grpc.demo.EnumTriggerReply;
import io.quarkus.ts.grpc.demo.EnumTriggerRequest;
import io.smallrye.mutiny.Uni;

@GrpcService
public class DemoEnumService implements Demo {

    private static final Logger LOG = Logger.getLogger(DemoEnumService.class);

    @Override
    public Uni<EnumTriggerReply> triggerEnumError(EnumTriggerRequest request) {
        return Uni.createFrom().item(request)
                .invoke(r -> {
                    LOG.info("Received request: " + r);
                })
                .map(msg -> EnumTriggerReply.newBuilder()
                        .setName(msg.getName())
                        .setEnum(msg.getEnum())
                        .build());
    }
}
