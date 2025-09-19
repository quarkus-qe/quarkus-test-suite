package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.refresh.clients;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;

import io.quarkus.oidc.client.filter.runtime.AbstractOidcClientRequestFilter;

@Priority(Priorities.AUTHENTICATION)
public class RefreshEnabledRequestFilter extends AbstractOidcClientRequestFilter {
    @Override
    protected boolean refreshOnUnauthorized() {
        return true;
    }
}
