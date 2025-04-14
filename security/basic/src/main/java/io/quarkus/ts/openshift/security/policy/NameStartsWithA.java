package io.quarkus.ts.openshift.security.policy;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class NameStartsWithA implements HttpSecurityPolicy {
    @Override
    public Uni<CheckResult> checkPermission(RoutingContext event, Uni<SecurityIdentity> identity,
            AuthorizationRequestContext requestContext) {
        return identity.map(id -> {
            boolean permit = id.getPrincipal().getName().startsWith("a");
            return new CheckResult(permit, id);
        });
    }

    @Override
    public String name() {
        return "alfa";
    }
}
