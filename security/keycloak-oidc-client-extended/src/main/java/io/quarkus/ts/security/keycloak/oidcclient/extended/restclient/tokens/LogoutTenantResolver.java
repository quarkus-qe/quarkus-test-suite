package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.tokens;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class LogoutTenantResolver implements TenantResolver {

    @Override
    public String resolve(RoutingContext context) {
        String path = context.normalizedPath();
        if (path.endsWith("code-flow") || path.endsWith("code-flow/logout")) {
            return "code-flow";
        }
        return null;
    }
}
