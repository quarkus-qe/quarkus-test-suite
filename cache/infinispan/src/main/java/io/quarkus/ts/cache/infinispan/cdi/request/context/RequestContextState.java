package io.quarkus.ts.cache.infinispan.cdi.request.context;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class RequestContextState {

    /**
     * We store this "state" in the CDI request context.
     * If the CDI request context propagation works as expected, then
     * Jakarta REST filter should be able to retrieve the same state (stored in this field)
     * as is returned from the cached method.
     */
    private volatile String state;

    String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
