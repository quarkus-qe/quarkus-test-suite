package io.quarkus.ts.reactive.rest.data.panache.secured;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.reactive.rest.data.panache.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/permissions-allowed-method")
public interface EntityPermissionsAllowedMethodResource extends PanacheEntityResource<ApplicationEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    boolean delete(Long id);

    @Override
    @PermissionsAllowed("count-1")
    @PermissionsAllowed("count-2")
    long count();

    @PermissionsAllowed("write")
    @GET
    @Path("/custom-count")
    default long customCount() {
        return ApplicationEntity.count();
    }
}
