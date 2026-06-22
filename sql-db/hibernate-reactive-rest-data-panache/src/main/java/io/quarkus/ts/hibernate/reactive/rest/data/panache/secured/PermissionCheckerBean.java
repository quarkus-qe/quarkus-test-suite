package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.security.PermissionChecker;
import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class PermissionCheckerBean {

    @PermissionChecker("read")
    boolean canRead(SecurityIdentity identity) {
        return identity.hasRole("admin") || identity.hasRole("user");
    }

    @PermissionChecker("write")
    boolean canWrite(SecurityIdentity identity) {
        return identity.hasRole("admin");
    }

    @PermissionChecker("delete")
    boolean canDelete(SecurityIdentity identity) {
        return identity.hasRole("user");
    }

    @PermissionChecker("count-1")
    boolean canCount1(SecurityIdentity identity) {
        return identity.hasRole("admin") || identity.hasRole("user");
    }

    @PermissionChecker("count-2")
    boolean canCount2(SecurityIdentity identity) {
        return identity.hasRole("admin");
    }
}
