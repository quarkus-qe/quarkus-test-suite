package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.oidc.common.OidcResponseFilter;

@BearerTokenAuthentication
@ApplicationScoped
public class BearerTokenResponseFilter implements OidcResponseFilter {
    private volatile boolean called = false;
    private volatile String contentType = null;

    @Override
    public void filter(OidcResponseContext responseContext) {
        this.called = true;
        this.contentType = responseContext.responseHeaders().get("Content-Type");
    }

    public boolean isCalled() {
        return called;
    }

    public String getContentType() {
        return contentType;
    }

    public void reset() {
        this.called = false;
        this.contentType = null;
    }
}
