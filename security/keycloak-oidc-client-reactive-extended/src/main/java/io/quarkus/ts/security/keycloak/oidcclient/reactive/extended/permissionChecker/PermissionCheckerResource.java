package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.permissionChecker;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.security.PermissionChecker;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;

@Path("/permission-checker")
public class PermissionCheckerResource {
    @PermissionsAllowed("project:rename")
    @GET
    @Path("/rename")
    public String renameProject(@RestQuery String projectName, @RestQuery String newName) {
        return "project " + projectName + " renamed to " + newName;
    }

    @Path("/delete")
    @GET
    @PermissionsAllowed(value = { "project:delete", "project:deletable" }, inclusive = true)
    public String deleteProject(@RestQuery String projectName) {
        return "project " + projectName + " deleted";
    }

    @PermissionChecker("project:rename")
    boolean canRenameProject(SecurityIdentity identity, String projectName) {
        return isMyProject(identity, projectName);
    }

    @PermissionChecker("project:delete")
    boolean canDeleteProject(SecurityIdentity identity, String projectName) {
        return isMyProject(identity, projectName);
    }

    @PermissionChecker("project:deletable")
    @Blocking
    boolean isProjectDeletable(SecurityIdentity identity, String projectName) {
        return projectName.contains("deletable");
    }

    private boolean isMyProject(SecurityIdentity identity, String projectName) {
        String principalName = identity.getPrincipal().getName();
        return projectName.contains(principalName);
    }
}
