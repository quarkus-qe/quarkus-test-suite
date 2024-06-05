package io.quarkus.ts.openshift.security.basic;

import jakarta.inject.Singleton;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

@Singleton
// if https://github.com/quarkusio/quarkus/issues/40307 is not fixed, then presence of a filter leads to 500 error
public class SecurityFilter {

    @RouteFilter(401)
    void filter(RoutingContext routingContext) {
        routingContext.next();
    }
}
