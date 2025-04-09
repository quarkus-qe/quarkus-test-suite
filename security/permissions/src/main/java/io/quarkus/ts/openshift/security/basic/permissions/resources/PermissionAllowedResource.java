package io.quarkus.ts.openshift.security.basic.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.openshift.security.basic.permissions.resources.helpers.CustomPermission;

@Path("/default-permission-allowed")
public class PermissionAllowedResource {

    public static final String RESPONSE_STRING = "Permitted";
    public static final String CUSTOM_PERMISSION_PATH = "/custom-permission";

    @GET
    @Path("/multiple-permission-annotation")
    @PermissionsAllowed("read:all")
    @PermissionsAllowed("create")
    public String multipleAnnotation() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/one-permission-annotation")
    @PermissionsAllowed({ "read:minimal", "read:all", "update" })
    public String oneAnnotation() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/one-permission-annotation-inclusive")
    @PermissionsAllowed(value = { "read:minimal", "read:all", "update" }, inclusive = true)
    public String oneAnnotationInclusive() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/multiple-annotation-multiple-permission")
    @PermissionsAllowed("read:minimal")
    @PermissionsAllowed({ "read:all", "create" })
    public String multipleAnnotationMultiplePermission() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/multiple-annotation-multiple-permission-inclusive")
    @PermissionsAllowed("read:minimal")
    @PermissionsAllowed(value = { "read:all", "create" }, inclusive = true)
    public String multipleAnnotationMultiplePermissionInclusive() {
        return RESPONSE_STRING;
    }

    @GET
    @Path(CUSTOM_PERMISSION_PATH + "/custom-permission")
    @PermissionsAllowed(value = { "read:minimal", "read:all", "update" }, permission = CustomPermission.class)
    public String oneAnnotationWithCustomPermission(@QueryParam("custom-permission") String parameter) {
        return RESPONSE_STRING + " query parameter: " + parameter;
    }

    @GET
    @Path(CUSTOM_PERMISSION_PATH + "/custom-permission-inclusive")
    @PermissionsAllowed(value = { "read:minimal", "read:all", "update" }, permission = CustomPermission.class, inclusive = true)
    public String oneAnnotationWithCustomPermissionInclusive(@QueryParam("custom-permission") String parameter) {
        return RESPONSE_STRING + " query parameter: " + parameter;
    }
}
