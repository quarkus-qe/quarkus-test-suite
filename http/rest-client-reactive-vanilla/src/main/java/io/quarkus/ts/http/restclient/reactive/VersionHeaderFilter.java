package io.quarkus.ts.http.restclient.reactive;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Used for {@link RestCallerService}
 */
@Provider
@PreMatching
@ApplicationScoped
public class VersionHeaderFilter implements ContainerResponseFilter {
    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    public VersionHeaderFilter() {
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Build-Version", applicationVersion);
    }
}
