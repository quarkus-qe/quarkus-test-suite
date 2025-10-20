package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.stepup;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.oidc.TenantResolver;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class StepUpAuthOidcTenantResolver implements TenantResolver {

    /**
     * Name of OIDC tenant with the 'gold' default ACR claim.
     */
    public static final String WEB_APP_GOLD = "web-app-gold";

    @Override
    public String resolve(RoutingContext context) {
        if (context.normalizedPath().endsWith("-web-app")) {
            if (WEB_APP_GOLD.equals(context.request().query()) || anyCookieNameContains(WEB_APP_GOLD, context)) {
                return WEB_APP_GOLD;
            }
            return "web-app-silver";
        }
        return null;
    }

    private static boolean anyCookieNameContains(String cookieNamePrefix, RoutingContext context) {
        return context.request().cookies().stream().anyMatch(c -> c.getName().contains(cookieNamePrefix));
    }

}
