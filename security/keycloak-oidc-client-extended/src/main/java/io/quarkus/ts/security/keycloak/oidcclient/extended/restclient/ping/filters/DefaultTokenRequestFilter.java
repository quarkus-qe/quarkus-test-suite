package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.ping.filters;

import io.quarkus.oidc.token.propagation.AccessTokenRequestFilter;

/**
 * This class is required for
 * {@link io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.ping.clients.TokenPropagationPongClient}
 * It would not be required normally, but having {@link CustomTokenRequestFilter} causes AmbiguousResolutionException when
 * getting a default filter.
 * So this class is necessary to have unambiguous filter for TokenPropagatingPongClient.
 */
public class DefaultTokenRequestFilter extends AccessTokenRequestFilter {
}
