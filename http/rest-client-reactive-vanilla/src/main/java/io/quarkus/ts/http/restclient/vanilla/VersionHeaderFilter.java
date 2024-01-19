package io.quarkus.ts.http.restclient.vanilla;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

/**
 * Used for {@link RestCallerService}
 * Does nothing we just need quarkus to have a possibility to use a ContainerResponseFilter
 */
@Provider
@PreMatching
@ApplicationScoped
public class VersionHeaderFilter implements ContainerResponseFilter {
    public VersionHeaderFilter() {
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // do nothing. implements this method just because ContainerResponseFilter requires it
    }
}
