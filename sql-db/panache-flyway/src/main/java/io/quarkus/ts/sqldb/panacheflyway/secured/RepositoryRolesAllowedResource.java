package io.quarkus.ts.sqldb.panacheflyway.secured;

import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.UserEntity;
import io.quarkus.ts.sqldb.panacheflyway.UserRepository;

@ResourceProperties(path = "/secured/repository/roles-allowed")
@RolesAllowed("admin")
public interface RepositoryRolesAllowedResource extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {
}
