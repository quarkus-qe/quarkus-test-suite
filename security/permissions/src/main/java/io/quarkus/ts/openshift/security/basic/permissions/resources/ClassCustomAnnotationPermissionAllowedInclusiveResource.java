package io.quarkus.ts.openshift.security.basic.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAllAndCreateInclusive;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadMinimal;

@Path("/class-custom-permission-allowed")
@CanReadAllAndCreateInclusive
public class ClassCustomAnnotationPermissionAllowedInclusiveResource {

    @GET
    @Path("/additional-permission-inclusive")
    @CanReadMinimal
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
