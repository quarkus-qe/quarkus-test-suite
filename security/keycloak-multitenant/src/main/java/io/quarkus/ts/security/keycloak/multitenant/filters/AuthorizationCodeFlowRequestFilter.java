package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.AuthorizationCodeFlow;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcRequestFilter;

@AuthorizationCodeFlow
@OidcEndpoint(OidcEndpoint.Type.TOKEN)
@ApplicationScoped
public class AuthorizationCodeFlowRequestFilter implements OidcRequestFilter {
    private volatile boolean called = false;

    @Override
    public void filter(OidcRequestContext requestContext) {
        this.called = true;
        requestContext.request().putHeader("X-Code-Flow-Filter", "executed");
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        this.called = false;
    }
}
