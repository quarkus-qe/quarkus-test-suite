package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.clients;

import java.util.Optional;

import io.quarkus.oidc.client.Tokens;
import io.quarkus.oidc.client.filter.runtime.AbstractOidcClientRequestFilter;
import io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens.TokenEchoResource;
import io.smallrye.mutiny.Uni;

public class TokenEchoRefreshFilter extends AbstractOidcClientRequestFilter {
    @Override
    protected Optional<String> clientId() {
        return Optional.of("async-token-refresh");
    }

    @Override
    public Uni<Tokens> getTokens() {
        TokenEchoResource.addTokenRefreshCount();
        return super.getTokens();
    }
}
