package io.quarkus.ts.reactive.rest.data.panache.secured;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.reactive.rest.data.panache.UserRepository;

@ResourceProperties(path = "/secured/repository/resource-properties-roles-allowed", rolesAllowed = "admin")
public interface RepositoryResourcePropertiesRolesAllowedResource
        extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {
}
