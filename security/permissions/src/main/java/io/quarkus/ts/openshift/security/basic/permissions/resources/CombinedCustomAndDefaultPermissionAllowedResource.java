package io.quarkus.ts.openshift.security.basic.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAllAndCreate;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAllAndCreateInclusive;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadMinimal;

@Path("/combined-permission-allowed")
public class CombinedCustomAndDefaultPermissionAllowedResource {

    public static final String RESPONSE_STRING = "Permitted";

    @GET
    @Path("/annotated-can-read-and-create")
    @PermissionsAllowed("read:minimal")
    @CanReadAllAndCreate
    public String annotatedCanReadAndCreate() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/annotated-can-read-and-create-inclusive")
    @PermissionsAllowed("read:minimal")
    @CanReadAllAndCreateInclusive
    public String annotatedCanReadAndCreateInclusive() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/annotated-can-read-minimal")
    @CanReadMinimal
    @PermissionsAllowed({ "read:all", "create" })
    public String annotatedCanReadMinimal() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/annotated-can-read-minimal-inclusive")
    @CanReadMinimal
    @PermissionsAllowed(value = { "read:all", "create" }, inclusive = true)
    public String annotatedCanReadMinimalInclusive() {
        return RESPONSE_STRING;
    }
}
