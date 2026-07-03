package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.rar;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class RarWrongTypeTenantResolver implements TenantResolver {

    @Override
    public String resolve(RoutingContext context) {
        String path = context.normalizedPath();
        if (path.startsWith("/rar-redirect-wrong/")) {
            return "rar-redirect-wrong";
        }
        if (path.startsWith("/rar-wrong/")) {
            return "rar-wrong";
        }
        return null;
    }
}
