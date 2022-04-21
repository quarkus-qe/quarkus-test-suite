package io.quarkus.ts.stork.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.smallrye.mutiny.Uni;
import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.ServiceInstance;
import io.smallrye.stork.impl.DefaultServiceInstance;
import io.smallrye.stork.utils.ServiceInstanceIds;

public class SimpleServiceDiscovery implements ServiceDiscovery {
    private static final Map<String, String> globalConfig = new HashMap<>();

    public SimpleServiceDiscovery(SimpleServiceDiscoveryProviderConfiguration configuration) {
        Optional
                .ofNullable(configuration.getPongServiceHost())
                .map(host -> globalConfig.put("pongHost", host));

        Optional
                .ofNullable(configuration.getPongServicePort())
                .map(port -> globalConfig.put("pongPort", port));

        Optional
                .ofNullable(configuration.getPongReplicaServiceHost())
                .map(host -> globalConfig.put("pongReplicaHost", host));

        Optional
                .ofNullable(configuration.getPongReplicaServicePort())
                .map(port -> globalConfig.put("pongReplicaPort", port));
    }

    @Override
    public Uni<List<ServiceInstance>> getServiceInstances() {
        DefaultServiceInstance pongInstance = new DefaultServiceInstance(ServiceInstanceIds.next(),
                globalConfig.get("pongHost"),
                Integer.parseInt(globalConfig.get("pongPort")),
                false);

        DefaultServiceInstance pongReplicaInstance = new DefaultServiceInstance(ServiceInstanceIds.next(),
                globalConfig.get("pongReplicaHost"),
                Integer.parseInt(globalConfig.get("pongReplicaPort")),
                false);

        return Uni.createFrom().item(() -> List.of(pongInstance, pongReplicaInstance));
    }
}
