package io.quarkus.ts.stork.custom;

import java.util.Collections;
import java.util.List;

import io.smallrye.mutiny.Uni;
import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.ServiceInstance;
import io.smallrye.stork.impl.DefaultServiceInstance;
import io.smallrye.stork.utils.ServiceInstanceIds;

public class SimpleServiceDiscovery implements ServiceDiscovery {
    private final String host;
    private final int port;

    public SimpleServiceDiscovery(InMemoryConfiguration configuration) {
        this.host = configuration.host();
        this.port = Integer.parseInt(configuration.port());
    }

    @Override
    public Uni<List<ServiceInstance>> getServiceInstances() {
        Long id = ServiceInstanceIds.next();
        DefaultServiceInstance instance = new DefaultServiceInstance(id, host, port, false);
        return Uni.createFrom().item(() -> Collections.singletonList(instance));
    }
}
