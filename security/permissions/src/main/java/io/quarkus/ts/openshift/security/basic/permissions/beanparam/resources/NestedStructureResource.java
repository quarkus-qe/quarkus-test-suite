package io.quarkus.ts.openshift.security.basic.permissions.beanparam.resources;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.openshift.security.basic.permissions.beanparam.NestedStructureBeanParam;
import io.quarkus.ts.openshift.security.basic.permissions.beanparam.permissions.NestedBeanParamPermission;

@Path("/bean-param/nested-structure")
@Produces(MediaType.TEXT_PLAIN)
public class NestedStructureResource {
    /**
     * Endpoint access to nested fields for documents.
     * Requires a valid username to access documents.
     */
    @GET
    @Path("/document")
    @PermissionsAllowed(value = "read", permission = NestedBeanParamPermission.class, params = {
            "nestedParam.id",
            "nestedParam.name",
            "nestedParam.resourceId",
            "nestedParam.resourceType",
            "nestedParam.principalName"
    })
    public String accessNestedDocumentsStructure(@BeanParam NestedStructureBeanParam nestedParam) {
        String userId = nestedParam.getUser().getId();
        String userName = nestedParam.getUser().getName();
        String resourceId = nestedParam.getResource().getDetails().getId();
        String resourceType = nestedParam.getResource().getDetails().getType();

        return String.format("Access granted to the document, User  %s ID: %s Resource %s (Type %s)",
                userName, userId, resourceId, resourceType);
    }

    /**
     * Endpoint to nested fields for profiles.
     * Requires user ID matches resource ID.
     */
    @GET
    @Path("/profile")
    @PermissionsAllowed(value = "read", permission = NestedBeanParamPermission.class, params = {
            "nestedParam.id",
            "nestedParam.name",
            "nestedParam.resourceId",
            "nestedParam.resourceType",
            "nestedParam.principalName"
    })
    public String accessNestedProfileStructure(@BeanParam NestedStructureBeanParam nestedParam) {
        String userId = nestedParam.getUser().getId();
        String userName = nestedParam.getUser().getName();
        String resourceId = nestedParam.getResource().details().id();

        return String.format("Access granted to profile. User: %s (ID: %s), Profile ID: %s",
                userName, userId, resourceId);
    }
}