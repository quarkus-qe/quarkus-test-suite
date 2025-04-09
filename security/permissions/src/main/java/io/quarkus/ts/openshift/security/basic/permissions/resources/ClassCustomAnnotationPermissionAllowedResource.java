package io.quarkus.ts.openshift.security.basic.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAllAndCreate;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadMinimal;

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
