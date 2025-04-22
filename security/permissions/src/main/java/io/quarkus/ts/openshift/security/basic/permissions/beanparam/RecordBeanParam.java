package io.quarkus.ts.openshift.security.basic.permissions.beanparam;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

public record RecordBeanParam(@QueryParam("docId") String documentId,
        @QueryParam("version") String version,
        @QueryParam("accessLevel") String accessLevel,
        @HeaderParam("CustomAuthorization") String customAuthorizationHeader,
        @Context SecurityContext securityContext,
        @Context UriInfo uriInfo) {

    public String getPrincipalName() {
        return securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : null;
    }
}
