package io.quarkus.ts.stork.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import io.smallrye.stork.api.LoadBalancer;
import io.smallrye.stork.api.NoServiceInstanceFoundException;
import io.smallrye.stork.api.ServiceInstance;

public class SimpleLoadBalancer implements LoadBalancer {

    private final Random random;

    public SimpleLoadBalancer(SimpleLoadBalancerProviderConfiguration config) {
        random = new Random();
    }

    @Override
    public ServiceInstance selectServiceInstance(Collection<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            throw new NoServiceInstanceFoundException("No services found.");
        }
        int index = random.nextInt(serviceInstances.size());
        return new ArrayList<>(serviceInstances).get(index);
    }
}
