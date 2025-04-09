package io.quarkus.ts.openshift.security.basic.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.PermissionsAllowed;

@Path("/class-permission-allowed")
@PermissionsAllowed({ "read:all", "create" })
public class ClassAnnotationPermissionAllowedResource {

    @GET
    @Path("/additional-permission")
    @PermissionsAllowed("read:minimal")
    public String additionalPermission() {
        // The method permission should override the class permission
        return "Permitted";
    }

    @GET
    @Path("/no-additional-permission")
    public String noAdditionalPermission() {
        return "Permitted by class annotation";
    }
}
