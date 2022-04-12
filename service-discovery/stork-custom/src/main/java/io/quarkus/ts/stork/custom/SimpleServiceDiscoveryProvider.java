package io.quarkus.ts.stork.custom;

import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.config.ServiceConfig;
import io.smallrye.stork.api.config.ServiceDiscoveryAttribute;
import io.smallrye.stork.api.config.ServiceDiscoveryType;
import io.smallrye.stork.spi.ServiceDiscoveryProvider;
import io.smallrye.stork.spi.StorkInfrastructure;

@ServiceDiscoveryType("simple")
@ServiceDiscoveryAttribute(name = "host",
        description = "Host name of the service discovery server.", required = true)
@ServiceDiscoveryAttribute(name = "port",
        description = "Hort of the service discovery server.", required = false)
public class SimpleServiceDiscoveryProvider implements ServiceDiscoveryProvider<InMemoryConfiguration> {

    @Override
    public ServiceDiscovery createServiceDiscovery(
            InMemoryConfiguration config,
            String serviceName,
            ServiceConfig serviceConfig,
            StorkInfrastructure storkInfrastructure) {
        return new SimpleServiceDiscovery(config);
    }
}
