package io.quarkus.ts.messaging.infinispan.grpc.kafka;

import static io.quarkus.ts.messaging.infinispan.grpc.kafka.LocalHostManagedResourceUtil.wrapWithLocalhostManagedResource;

import io.quarkus.test.bootstrap.ManagedResource;
import io.quarkus.test.bootstrap.ServiceContext;
import io.quarkus.test.services.containers.ContainerManagedResourceBuilder;

public class LocalHostInfinispanManagedResourceBuilder extends ContainerManagedResourceBuilder {
    @Override
    public ManagedResource build(ServiceContext context) {
        return wrapWithLocalhostManagedResource(super.build(context));
    }
}
