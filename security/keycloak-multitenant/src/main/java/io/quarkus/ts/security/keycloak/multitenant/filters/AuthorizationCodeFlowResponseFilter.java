package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.AuthorizationCodeFlow;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcResponseFilter;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;

@AuthorizationCodeFlow
@OidcEndpoint(OidcEndpoint.Type.TOKEN)
@ApplicationScoped
public class AuthorizationCodeFlowResponseFilter implements OidcResponseFilter {
    private volatile boolean called = false;
    private volatile String contentType = null;

    @Override
    public Uni<Void> filter(OidcResponseFilterContext responseContext) {
        this.called = true;
        this.contentType = responseContext.responseHeaders().get(HttpHeaders.CONTENT_TYPE);
        return Uni.createFrom().voidItem();
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isCalled() {
        return called;
    }

    public void reset() {
        this.called = false;
        this.contentType = null;
    }

}
