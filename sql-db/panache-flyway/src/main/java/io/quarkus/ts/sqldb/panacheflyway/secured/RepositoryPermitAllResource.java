package io.quarkus.ts.sqldb.panacheflyway.secured;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.UserEntity;
import io.quarkus.ts.sqldb.panacheflyway.UserRepository;

@ResourceProperties(path = "/secured/repository/permit-all")
@PermitAll
public interface RepositoryPermitAllResource extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {
    @Override
    @DenyAll
    long count();
}
