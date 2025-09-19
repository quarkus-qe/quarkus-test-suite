package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.tokens.refresh.clients;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;

import io.quarkus.oidc.client.reactive.filter.runtime.AbstractOidcClientRequestReactiveFilter;

@Priority(Priorities.AUTHENTICATION)
public class RefreshEnabledRequestFilter extends AbstractOidcClientRequestReactiveFilter {
    @Override
    protected boolean refreshOnUnauthorized() {
        return true;
    }
}
