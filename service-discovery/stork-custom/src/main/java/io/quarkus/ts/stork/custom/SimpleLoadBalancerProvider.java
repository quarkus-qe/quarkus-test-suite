package io.quarkus.ts.stork.custom;

import io.smallrye.stork.api.LoadBalancer;
import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.config.LoadBalancerType;
import io.smallrye.stork.spi.LoadBalancerProvider;

@LoadBalancerType("simple")
public class SimpleLoadBalancerProvider implements LoadBalancerProvider<SimpleLoadBalancerProviderConfiguration> {
    @Override
    public LoadBalancer createLoadBalancer(SimpleLoadBalancerProviderConfiguration config,
            ServiceDiscovery serviceDiscovery) {
        return new SimpleLoadBalancer(config);
    }
}
