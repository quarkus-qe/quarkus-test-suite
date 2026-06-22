package io.quarkus.ts.reactive.rest.data.panache.secured;

import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.reactive.rest.data.panache.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/roles-permissions-override")
@RolesAllowed("admin")
public interface EntityRolesAllowedPermissionsOverrideResource extends PanacheEntityResource<ApplicationEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    boolean delete(Long id);
}
