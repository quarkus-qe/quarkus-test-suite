package io.quarkus.ts.security.permissions.resources.helpers;

import java.security.BasicPermission;
import java.security.Permission;

import io.quarkus.arc.Arc;
import io.vertx.ext.web.RoutingContext;

public class CustomPermission extends BasicPermission {

    public CustomPermission(String name) {
        super(name);
    }

    @Override
    public boolean implies(Permission permission) {
        try (var event = Arc.container().instance(RoutingContext.class)) {
            if (event == null) {
                throw new RuntimeException("The event should not be null. Arc couldn't find RoutingContext instance");
            }
            var publicContent = "custom-permission".equals(event.get().request().params().get("custom-permission"));
            var hasPermission = getName().equals(permission.getName());
            return hasPermission && publicContent;
        }
    }
}
