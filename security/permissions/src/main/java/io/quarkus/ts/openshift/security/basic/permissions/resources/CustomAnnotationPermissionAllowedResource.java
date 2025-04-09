package io.quarkus.ts.openshift.security.basic.permissions.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanCreate;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAll;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAllAndCreate;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAllAndCreateInclusive;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAndUpdate;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAndUpdateCustomPermission;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAndUpdateCustomPermissionInclusive;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadAndUpdateInclusive;
import io.quarkus.ts.openshift.security.basic.permissions.annotations.CanReadMinimal;

@Path("/custom-annotation-permission-allowed")
public class CustomAnnotationPermissionAllowedResource {

    public static final String RESPONSE_STRING = "Permitted";
    public static final String CUSTOM_PERMISSION_PATH = "/custom-permission";

    @GET
    @Path("/multiple-permission-annotation")
    @CanReadAll
    @CanCreate
    public String multipleAnnotation() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/one-permission-annotation")
    @CanReadAndUpdate
    public String oneAnnotation() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/one-permission-annotation-inclusive")
    @CanReadAndUpdateInclusive
    public String oneAnnotationInclusive() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/multiple-annotation-multiple-permission")
    @CanReadMinimal
    @CanReadAllAndCreate
    public String multipleAnnotationMultiplePermission() {
        return RESPONSE_STRING;
    }

    @GET
    @Path("/multiple-annotation-multiple-permission-inclusive")
    @CanReadMinimal
    @CanReadAllAndCreateInclusive
    public String multipleAnnotationMultiplePermissionInclusive() {
        return RESPONSE_STRING;
    }

    @GET
    @Path(CUSTOM_PERMISSION_PATH + "/custom-permission")
    @CanReadAndUpdateCustomPermission
    public String oneAnnotationWithCustomPermission(@QueryParam("custom-permission") String parameter) {
        return RESPONSE_STRING + " query parameter: " + parameter;
    }

    @GET
    @Path(CUSTOM_PERMISSION_PATH + "/custom-permission-inclusive")
    @CanReadAndUpdateCustomPermissionInclusive
    public String oneAnnotationWithCustomPermissionInclusive(@QueryParam("custom-permission") String parameter) {
        return RESPONSE_STRING + " query parameter: " + parameter;
    }
}
