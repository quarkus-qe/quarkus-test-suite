package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantFeature;
import io.quarkus.oidc.common.OidcRequestFilter;
import io.smallrye.mutiny.Uni;

@TenantFeature({ "service-tenant", "jwt-tenant" })
@ApplicationScoped
public class MultiTenantFeatureFilter implements OidcRequestFilter {

    private volatile boolean called = false;

    @Override
    public Uni<Void> filter(OidcRequestFilterContext requestContext) {
        this.called = true;
        return Uni.createFrom().voidItem();
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        this.called = false;
    }
}
