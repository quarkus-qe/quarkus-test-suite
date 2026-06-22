package io.quarkus.ts.reactive.rest.data.panache.secured;

import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.reactive.rest.data.panache.UserRepository;

@ResourceProperties(path = "/secured/repository/roles-permissions-override")
@RolesAllowed("admin")
public interface RepositoryRolesAllowedPermissionsOverrideResource
        extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    boolean delete(Long id);
}
