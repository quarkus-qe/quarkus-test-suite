package io.quarkus.ts.security.keycloak.multitenant;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class CustomTenantResolver implements TenantResolver {

    @Override
    public String resolve(RoutingContext context) {
        String path = context.request().path();

        return Stream.of(Tenant.values())
                .map(Tenant::getValue)
                .filter(path::contains)
                .findFirst()
                .orElse(null);
    }
}
