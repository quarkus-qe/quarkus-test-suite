package io.quarkus.ts.security.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.PermissionsAllowed;

@Path("/class-permission-allowed")
@PermissionsAllowed(value = { "read:all", "create" }, inclusive = true)
public class ClassAnnotationPermissionAllowedInclusiveResource {

    @GET
    @Path("/additional-permission-inclusive")
    @PermissionsAllowed("read:minimal")
    public String additionalPermission() {
        // The method permission should override the class permission
        return "Permitted";
    }

    @GET
    @Path("/no-additional-permission-inclusive")
    public String noAdditionalPermission() {
        return "Permitted by class annotation";
    }
}
