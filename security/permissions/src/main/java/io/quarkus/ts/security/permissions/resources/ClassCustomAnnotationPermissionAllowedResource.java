package io.quarkus.ts.security.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.ts.security.permissions.annotations.CanReadAllAndCreate;
import io.quarkus.ts.security.permissions.annotations.CanReadMinimal;

@Path("/class-custom-permission-allowed")
@CanReadAllAndCreate
public class ClassCustomAnnotationPermissionAllowedResource {

    @GET
    @Path("/additional-permission")
    @CanReadMinimal
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
