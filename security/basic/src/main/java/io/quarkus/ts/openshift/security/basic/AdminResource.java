package io.quarkus.ts.openshift.security.basic;

import java.util.Objects;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.SecurityContext;

@Path("/admin")
@RolesAllowed("admin")
public class AdminResource {

    private volatile SecurityContext securityContext = null;

    @Inject
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @GET
    public String get() {
        Objects.requireNonNull(securityContext, "'SecurityContext' is supposed to be injected");
        var response = "Hello, admin " + securityContext.getUserPrincipal().getName();
        // this verifies that security context injection has request scope as this endpoint
        // is called multiple times
        securityContext = null;
        return response;
    }
}
