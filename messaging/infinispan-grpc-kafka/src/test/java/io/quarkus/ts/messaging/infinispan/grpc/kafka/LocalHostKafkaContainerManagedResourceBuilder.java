package io.quarkus.ts.messaging.infinispan.grpc.kafka;

import static io.quarkus.ts.messaging.infinispan.grpc.kafka.LocalHostManagedResourceUtil.wrapWithLocalhostManagedResource;

import io.quarkus.test.bootstrap.ManagedResource;
import io.quarkus.test.bootstrap.ServiceContext;
import io.quarkus.test.services.containers.KafkaContainerManagedResourceBuilder;

public class LocalHostKafkaContainerManagedResourceBuilder extends KafkaContainerManagedResourceBuilder {
    @Override
    public ManagedResource build(ServiceContext context) {
        return wrapWithLocalhostManagedResource(super.build(context));
    }
}
