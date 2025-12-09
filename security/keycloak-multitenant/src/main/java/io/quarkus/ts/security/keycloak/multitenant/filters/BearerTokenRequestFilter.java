package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.oidc.common.OidcRequestFilter;

@BearerTokenAuthentication
@ApplicationScoped
public class BearerTokenRequestFilter implements OidcRequestFilter {
    private volatile boolean called = false;

    @Override
    public void filter(OidcRequestContext requestContext) {
        called = true;
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        called = false;
    }
}
