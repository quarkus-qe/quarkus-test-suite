package io.quarkus.ts.reactive.rest.data.panache.secured;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.reactive.rest.data.panache.UserRepository;

@ResourceProperties(path = "/secured/repository/permissions-allowed-method")
public interface RepositoryPermissionsAllowedMethodResource
        extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    boolean delete(Long id);

    @Override
    @PermissionsAllowed("count-1")
    @PermissionsAllowed("count-2")
    long count();
}
