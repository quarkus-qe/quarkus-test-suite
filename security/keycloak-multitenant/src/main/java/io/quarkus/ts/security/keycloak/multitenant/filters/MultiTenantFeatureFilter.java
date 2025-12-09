package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantFeature;
import io.quarkus.oidc.common.OidcRequestFilter;

@TenantFeature({ "service-tenant", "jwt-tenant" })
@ApplicationScoped
public class MultiTenantFeatureFilter implements OidcRequestFilter {

    private volatile boolean called = false;

    @Override
    public void filter(OidcRequestContext requestContext) {
        this.called = true;
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        this.called = false;
    }
}
