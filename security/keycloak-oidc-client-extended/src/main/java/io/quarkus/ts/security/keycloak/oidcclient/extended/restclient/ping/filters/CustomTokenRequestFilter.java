package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.ping.filters;

import io.quarkus.oidc.token.propagation.AccessTokenRequestFilter;

public class CustomTokenRequestFilter extends AccessTokenRequestFilter {
    @Override
    protected String getClientName() {
        return "exchange-token";
    }

    @Override
    protected boolean isExchangeToken() {
        return true;
    }
}
