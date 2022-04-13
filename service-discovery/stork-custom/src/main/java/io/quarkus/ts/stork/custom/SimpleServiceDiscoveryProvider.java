package io.quarkus.ts.stork.custom;

import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.config.ServiceConfig;
import io.smallrye.stork.api.config.ServiceDiscoveryAttribute;
import io.smallrye.stork.api.config.ServiceDiscoveryType;
import io.smallrye.stork.spi.ServiceDiscoveryProvider;
import io.smallrye.stork.spi.StorkInfrastructure;

@ServiceDiscoveryType("simple")
@ServiceDiscoveryAttribute(name = "pongServiceHost", description = "Host name of the pong service instance.", required = true)
@ServiceDiscoveryAttribute(name = "pongServicePort", description = "Port of the pong service instance.", required = false)
@ServiceDiscoveryAttribute(name = "pongReplicaServiceHost", description = "Host name of the pong service instance.", required = true)
@ServiceDiscoveryAttribute(name = "pongReplicaServicePort", description = "Port of the pong service instance.", required = false)
public class SimpleServiceDiscoveryProvider implements ServiceDiscoveryProvider<SimpleServiceDiscoveryProviderConfiguration> {
    @Override
    public ServiceDiscovery createServiceDiscovery(
            SimpleServiceDiscoveryProviderConfiguration config,
            String serviceName,
            ServiceConfig serviceConfig,
            StorkInfrastructure storkInfrastructure) {
        return new SimpleServiceDiscovery(config);
    }
}
