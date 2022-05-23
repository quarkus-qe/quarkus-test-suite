package io.quarkus.ts.stork.custom;

import io.smallrye.stork.api.LoadBalancer;
import io.smallrye.stork.api.ServiceDiscovery;
import io.smallrye.stork.api.config.LoadBalancerType;
import io.smallrye.stork.spi.LoadBalancerProvider;

@LoadBalancerType("simplelb")
public class SimpleLoadBalancerProvider implements LoadBalancerProvider<SimplelbConfiguration> {
    @Override
    public LoadBalancer createLoadBalancer(SimplelbConfiguration config,
            ServiceDiscovery serviceDiscovery) {
        return new SimpleLoadBalancer(config);
    }
}
