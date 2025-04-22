package io.quarkus.ts.openshift.security.basic.permissions.beanparam;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

public class SimpleBeanParam {
    @HeaderParam("CustomAuthorization")
    private String customAuthorizationHeader;

    @QueryParam("resourceId")
    public String resourceId;

    @QueryParam("action")
    private String action;

    @Context
    SecurityContext securityContext;

    @Context
    public UriInfo uriInfo;

    public String getCustomAuthorizationHeader() {
        return customAuthorizationHeader;
    }

    public String getAction() {
        return action;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public String getPrincipalName() {
        return securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : null;
    }
}
