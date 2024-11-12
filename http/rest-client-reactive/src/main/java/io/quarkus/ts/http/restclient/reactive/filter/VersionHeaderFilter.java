package io.quarkus.ts.http.restclient.reactive.filter;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Used to reproduce issue in https://github.com/quarkusio/quarkus/pull/37268
 * Needs to implement both ContainerResponseFilter and ClientRequestFilter to reproduce the issue,
 * although ContainerResponseFilter is ignored
 */
@Provider
public class VersionHeaderFilter implements ContainerResponseFilter, ClientRequestFilter {
    public VersionHeaderFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // do nothing. implements this method just because ContainerResponseFilter requires it
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().add("clientFilter", "clientFilterInvoked");
    }
}
