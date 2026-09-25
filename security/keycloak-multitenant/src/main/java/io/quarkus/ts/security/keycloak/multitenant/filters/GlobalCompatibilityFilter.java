package io.quarkus.ts.security.keycloak.multitenant.filters;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.common.OidcRequestFilter;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class GlobalCompatibilityFilter implements OidcRequestFilter {
    private final AtomicInteger invocationCount = new AtomicInteger(0);
    private volatile String lastTenantId = null;

    @Override
    public Uni<Void> filter(OidcRequestFilterContext requestContext) {
        int count = invocationCount.incrementAndGet();
        lastTenantId = requestContext.contextProperties()
                .getString(io.quarkus.oidc.runtime.OidcUtils.TENANT_ID_ATTRIBUTE);
        return Uni.createFrom().voidItem();

    }

    public boolean isCalled() {
        return invocationCount.get() > 0;
    }

    public int getInvocationCount() {
        return invocationCount.get();
    }

    public String getLastTenantId() {
        return lastTenantId;
    }

    public void reset() {
        invocationCount.set(0);
        lastTenantId = null;
    }
}
