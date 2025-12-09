package io.quarkus.ts.security.keycloak.multitenant.filters;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.BearerTokenAuthentication;
import io.quarkus.oidc.TenantFeature;
import io.quarkus.oidc.common.OidcEndpoint;
import io.quarkus.oidc.common.OidcRequestFilter;
import io.quarkus.oidc.runtime.OidcUtils;

@BearerTokenAuthentication
@TenantFeature("service-tenant")
@OidcEndpoint(OidcEndpoint.Type.INTROSPECTION)
@ApplicationScoped
public class CombinedBearerServiceFilter implements OidcRequestFilter {
    private volatile boolean called = false;
    private volatile String capturedTenantId = null;

    @Override
    public void filter(OidcRequestContext requestContext) {
        this.called = true;
        this.capturedTenantId = requestContext.contextProperties()
                .getString(OidcUtils.TENANT_ID_ATTRIBUTE);
        requestContext.request().putHeader("X-Combined-Filter", "executed");
    }

    public boolean isCalled() {
        return called;
    }

    public String getCapturedTenantId() {
        return capturedTenantId;
    }

    public void reset() {
        this.called = false;
        this.capturedTenantId = null;
    }
}
