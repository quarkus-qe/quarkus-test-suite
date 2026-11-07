package io.quarkus.ts.mcp.app;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

/**
 * Lets any authenticated user use the MCP server, but reserves protocol detection ({@code server/discover}) for admins,
 * so a token can be accepted for the legacy {@code initialize} but rejected for the detection request.
 */
@ApplicationScoped
public class DiscoverPolicy implements HttpSecurityPolicy {
    public static final String NAME = "discover-admin-only";

    @Override
    public Uni<CheckResult> checkPermission(RoutingContext request, Uni<SecurityIdentity> identity,
            AuthorizationRequestContext requestContext) {
        boolean discover = "server/discover".equals(request.request().getHeader("Mcp-Method"));
        return identity.map(user -> {
            if (user.isAnonymous()) {
                return CheckResult.DENY;
            }
            if (discover && !user.hasRole("test-admin-role")) {
                return CheckResult.DENY;
            }
            return CheckResult.PERMIT;
        });
    }

    @Override
    public String name() {
        return NAME;
    }
}
