package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.reactive.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.ApplicationEntity;
import io.smallrye.mutiny.Uni;

@ResourceProperties(path = "/secured/entity/roles-permissions-override")
@RolesAllowed("admin")
public interface EntityRolesAllowedPermissionsOverrideResource extends PanacheEntityResource<ApplicationEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    Uni<Boolean> delete(Long id);
}
