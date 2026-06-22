package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import io.quarkus.hibernate.reactive.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.UserRepository;

@ResourceProperties(path = "/secured/repository/permissions-allowed")
@PermissionsAllowed("read")
public interface RepositoryPermissionsAllowedResource
        extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {
}
