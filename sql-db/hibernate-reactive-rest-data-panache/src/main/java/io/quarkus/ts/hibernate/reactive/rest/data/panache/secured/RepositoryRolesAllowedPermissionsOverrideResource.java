package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.reactive.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.UserRepository;
import io.smallrye.mutiny.Uni;

@ResourceProperties(path = "/secured/repository/roles-permissions-override")
@RolesAllowed("admin")
public interface RepositoryRolesAllowedPermissionsOverrideResource
        extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    Uni<Boolean> delete(Long id);
}
