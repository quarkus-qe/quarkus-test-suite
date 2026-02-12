package io.quarkus.ts.scheduling.quartz.security;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScheduledSecurityCounters {

    private final AtomicInteger authenticatedSuccessCount = new AtomicInteger();
    private final AtomicInteger authenticatedFailureCount = new AtomicInteger();

    private final AtomicInteger adminSuccessCount = new AtomicInteger();
    private final AtomicInteger adminFailureNoIdentityCount = new AtomicInteger();
    private final AtomicInteger adminFailureWrongRoleCount = new AtomicInteger();

    private final AtomicInteger permitAllSuccessCount = new AtomicInteger();

    public void incrementAuthenticatedSuccess() {
        authenticatedSuccessCount.incrementAndGet();
    }

    public void incrementAuthenticatedFailure() {
        authenticatedFailureCount.incrementAndGet();
    }

    public void incrementAdminSuccess() {
        adminSuccessCount.incrementAndGet();
    }

    public void incrementAdminFailureNoIdentity() {
        adminFailureNoIdentityCount.incrementAndGet();
    }

    public void incrementAdminFailureWrongRole() {
        adminFailureWrongRoleCount.incrementAndGet();
    }

    public void incrementPermitAllSuccess() {
        permitAllSuccessCount.incrementAndGet();
    }

    public int getAuthenticatedSuccessCount() {
        return authenticatedSuccessCount.get();
    }

    public int getAuthenticatedFailureCount() {
        return authenticatedFailureCount.get();
    }

    public int getAdminSuccessCount() {
        return adminSuccessCount.get();
    }

    public int getAdminFailureNoIdentityCount() {
        return adminFailureNoIdentityCount.get();
    }

    public int getAdminFailureWrongRoleCount() {
        return adminFailureWrongRoleCount.get();
    }

    public int getPermitAllSuccessCount() {
        return permitAllSuccessCount.get();
    }
}
