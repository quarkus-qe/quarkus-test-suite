package io.quarkus.ts.reactive.rest.data.panache.secured;

import jakarta.annotation.security.DenyAll;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.reactive.rest.data.panache.UserEntity;
import io.quarkus.ts.reactive.rest.data.panache.UserRepository;

@ResourceProperties(path = "/secured/repository/public")
public interface RepositoryPublicResource extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {
    @Override
    @DenyAll
    long count();
}
