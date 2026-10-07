package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.oidc.common.OidcRequestFilter;
import io.smallrye.mutiny.Uni;

@BearerTokenAuthentication
@ApplicationScoped
public class BearerTokenRequestFilter implements OidcRequestFilter {
    private volatile boolean called = false;

    @Override
    public Uni<Void> filter(OidcRequestFilterContext requestContext) {
        called = true;
        return Uni.createFrom().voidItem();
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        called = false;
    }
}
