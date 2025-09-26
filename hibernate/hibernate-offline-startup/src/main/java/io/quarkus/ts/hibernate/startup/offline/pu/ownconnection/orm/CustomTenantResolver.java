package io.quarkus.ts.hibernate.startup.offline.pu.ownconnection.orm;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;

@ApplicationScoped
@PersistenceUnitExtension("own_connection_provider")
public class CustomTenantResolver implements TenantResolver {
    @Override
    public String getDefaultTenantId() {
        return "own_connection_provider";
    }

    @Override
    public String resolveTenantId() {
        return "own_connection_provider";
    }
}
