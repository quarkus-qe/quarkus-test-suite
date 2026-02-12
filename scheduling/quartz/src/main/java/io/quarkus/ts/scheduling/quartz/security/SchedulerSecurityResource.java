package io.quarkus.ts.scheduling.quartz.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/scheduled-security")
@ApplicationScoped
public class SchedulerSecurityResource {

    @Inject
    ScheduledSecurityCounters counters;

    @GET
    @Path("/authenticated/success")
    public int authenticatedSuccess() {
        return counters.getAuthenticatedSuccessCount();
    }

    @GET
    @Path("/authenticated/failure")
    public int authenticatedFailure() {
        return counters.getAuthenticatedFailureCount();
    }

    @GET
    @Path("/admin/success")
    public int adminSuccess() {
        return counters.getAdminSuccessCount();
    }

    @GET
    @Path("/admin/failure/no-identity")
    public int adminFailureNoIdentity() {
        return counters.getAdminFailureNoIdentityCount();
    }

    @GET
    @Path("/admin/failure/wrong-role")
    public int adminFailureWrongRole() {
        return counters.getAdminFailureWrongRoleCount();
    }

    @GET
    @Path("/permit-all/success")
    public int permitAllSuccess() {
        return counters.getPermitAllSuccessCount();
    }
}
