package io.quarkus.ts.scheduling.quartz.security;

import static io.quarkus.scheduler.Scheduled.QUARTZ;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.security.identity.RunAsUser;

@ApplicationScoped
public class ScheduledSecurityJobs {

    @Inject
    ProtectedService protectedService;

    @Inject
    ScheduledSecurityCounters counters;

    @Scheduled(every = "1s", executeWith = QUARTZ)
    void authenticatedWithoutIdentity() {
        try {
            protectedService.performAuthenticatedOperation();
        } catch (Exception e) {
            counters.incrementAuthenticatedFailure();
        }
    }

    @RunAsUser(user = "alice")
    @Scheduled(every = "1s", executeWith = QUARTZ)
    void authenticatedWithRunAsUser() {
        protectedService.performAuthenticatedOperation();
        counters.incrementAuthenticatedSuccess();
    }

    @RunAsUser(user = "admin", roles = "admin")
    @Scheduled(every = "1s", executeWith = QUARTZ)
    void adminOperationWithProperRole() {
        protectedService.performAdminOperation();
        counters.incrementAdminSuccess();
    }

    @Scheduled(every = "1s", executeWith = QUARTZ)
    void adminOperationWithoutIdentity() {
        try {
            protectedService.performAdminOperation();
        } catch (Exception e) {
            counters.incrementAdminFailureNoIdentity();
        }
    }

    @RunAsUser(user = "bob", roles = "user")
    @Scheduled(every = "1s", executeWith = QUARTZ)
    void adminOperationWithWrongRole() {
        try {
            protectedService.performAdminOperation();
        } catch (Exception e) {
            counters.incrementAdminFailureWrongRole();
        }
    }

    @Scheduled(every = "1s", executeWith = QUARTZ)
    void permitAllAccessScenario() {
        protectedService.performPermitAllOperation();
        counters.incrementPermitAllSuccess();
    }
}
