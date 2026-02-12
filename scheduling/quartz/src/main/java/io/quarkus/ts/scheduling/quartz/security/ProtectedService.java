package io.quarkus.ts.scheduling.quartz.security;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.security.Authenticated;

@ApplicationScoped
@RolesAllowed("admin")
public class ProtectedService {

    @Authenticated
    public void performAuthenticatedOperation() {
    }

    public void performAdminOperation() {
    }

    @PermitAll
    public void performPermitAllOperation() {
    }
}
