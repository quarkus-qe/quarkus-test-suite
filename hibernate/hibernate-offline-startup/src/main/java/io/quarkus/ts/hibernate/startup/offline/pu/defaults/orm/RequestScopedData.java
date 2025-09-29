package io.quarkus.ts.hibernate.startup.offline.pu.defaults.orm;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import io.vertx.ext.web.RoutingContext;

@RequestScoped
public final class RequestScopedData {

    private volatile String user;
    private volatile String password;

    @Inject
    RoutingContext routingContext;

    void loadCredentials() {
        user = routingContext.request().getHeader("username");
        password = routingContext.request().getHeader("password");
    }

    String getUser() {
        return user;
    }

    String getPassword() {
        return password;
    }

    boolean foundCredentials() {
        return password != null && !password.isEmpty();
    }
}
