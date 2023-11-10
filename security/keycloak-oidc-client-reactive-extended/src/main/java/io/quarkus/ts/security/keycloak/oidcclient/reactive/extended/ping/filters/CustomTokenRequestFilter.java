package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.ping.filters;

import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;

public class CustomTokenRequestFilter extends AccessTokenRequestReactiveFilter {
    @Override
    protected String getClientName() {
        return "exchange-token";
    }

    @Override
    protected boolean isExchangeToken() {
        return true;
    }
}
