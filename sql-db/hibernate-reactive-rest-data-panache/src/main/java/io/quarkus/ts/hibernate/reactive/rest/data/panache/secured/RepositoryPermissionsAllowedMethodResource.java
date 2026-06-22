package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import io.quarkus.hibernate.reactive.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.UserRepository;
import io.smallrye.mutiny.Uni;

@ResourceProperties(path = "/secured/repository/permissions-allowed-method")
public interface RepositoryPermissionsAllowedMethodResource
        extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    Uni<Boolean> delete(Long id);

    @Override
    @PermissionsAllowed("count-1")
    @PermissionsAllowed("count-2")
    Uni<Long> count();
}
