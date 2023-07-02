package io.quarkus.ts.sqldb.multiplepus;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;

@PersistenceUnitExtension("fungi")
@RequestScoped
public class FungiTenantResolver implements TenantResolver {

    public static final String TENANT_HEADER = "tenantId";

    @Inject
    HttpHeaders headers;

    @Override
    public String getDefaultTenantId() {
        return "Sarcoscypha";
    }

    @Override
    public String resolveTenantId() {
        var tenantId = headers.getHeaderString(TENANT_HEADER);
        if (tenantId != null && !tenantId.isEmpty()) {
            return tenantId;
        }

        return getDefaultTenantId();
    }

}
