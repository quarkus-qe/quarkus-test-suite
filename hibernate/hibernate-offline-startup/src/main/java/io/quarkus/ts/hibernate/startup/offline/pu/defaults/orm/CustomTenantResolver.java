package io.quarkus.ts.hibernate.startup.offline.pu.defaults.orm;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.logging.Log;

@PersistenceUnitExtension
@RequestScoped
public class CustomTenantResolver implements TenantResolver {

    @Inject
    UriInfo uriInfo;

    @Inject
    RequestScopedData requestScopedData;

    @Override
    public String getDefaultTenantId() {
        return resolveTenantIdInternal();
    }

    @Override
    public String resolveTenantId() {
        return resolveTenantIdInternal();
    }

    private String resolveTenantIdInternal() {
        requestScopedData.loadCredentials();
        String tenant = uriInfo.getPathParameters().getFirst("tenant");
        if (tenant != null && !tenant.isEmpty()) {
            Log.debug("Resolved tenant " + tenant);
            return tenant;
        }
        throw new IllegalStateException("Tenant not found");
    }
}
