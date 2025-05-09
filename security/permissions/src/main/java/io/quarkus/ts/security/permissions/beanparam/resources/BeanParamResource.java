package io.quarkus.ts.security.permissions.beanparam.resources;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.security.permissions.beanparam.CommonFieldBeanParam;
import io.quarkus.ts.security.permissions.beanparam.RecordBeanParam;
import io.quarkus.ts.security.permissions.beanparam.SimpleBeanParam;
import io.quarkus.ts.security.permissions.beanparam.permissions.CommonFieldPermission;
import io.quarkus.ts.security.permissions.beanparam.permissions.RecordBeanParamPermission;
import io.quarkus.ts.security.permissions.beanparam.permissions.SimpleBeanParamPermission;

@Path("/bean-param")
public class BeanParamResource {
    @GET
    @Path("/simple")
    @PermissionsAllowed(value = "read", permission = SimpleBeanParamPermission.class, params = {
            "beanParam.resourceId",
            "beanParam.customAuthorizationHeader",
            "beanParam.action",
            "beanParam.principalName"
    })
    public String simpleAccess(@BeanParam SimpleBeanParam beanParam) {
        return "Simple access granted to resource " + beanParam.resourceId +
                " from path " + beanParam.uriInfo.getPath();
    }

    @GET
    @Path("/record")
    @PermissionsAllowed(value = "read", permission = RecordBeanParamPermission.class, params = {
            "recordParam.documentId",
            "recordParam.customAuthorizationHeader",
            "recordParam.accessLevel",
            "recordParam.principalName"
    })
    public String recordAccess(@BeanParam RecordBeanParam recordParam) {
        String path = "/bean-param/record";
        if (recordParam.uriInfo() != null) {
            path = recordParam.uriInfo().getPath();
        }
        return "Record access granted to document " + recordParam.documentId() +
                " from path " + path;
    }

    @GET
    @Path("/common-field")
    @PermissionsAllowed(value = "read", permission = CommonFieldPermission.class, params = {
            "commonFieldBeanParam.customAuthorizationHeader",
            "commonFieldBeanParam.principalName"
    })
    public String commonField(@BeanParam CommonFieldBeanParam commonFieldBeanParam) {
        return "Common field access granted for operation " + commonFieldBeanParam.operation +
                " from path " + commonFieldBeanParam.uriInfo.getPath();
    }

    @POST
    @Path("/write")
    @Consumes(MediaType.TEXT_PLAIN)
    @PermissionsAllowed(value = "write", permission = SimpleBeanParamPermission.class, params = {
            "beanParam.resourceId",
            "beanParam.customAuthorizationHeader",
            "beanParam.action",
            "beanParam.principalName"
    })
    public String writeAccess(@BeanParam SimpleBeanParam beanParam, String content) {
        return "Write successful to resource " + beanParam.resourceId +
                " from path " + beanParam.uriInfo.getPath();
    }
}