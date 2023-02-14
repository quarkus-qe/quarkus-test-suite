package io.quarkus.ts.sqldb.panacheflyway.secured;

import java.util.List;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.UserEntity;
import io.quarkus.ts.sqldb.panacheflyway.UserRepository;

@ResourceProperties(path = "/secured/repository/deny-all")
@DenyAll
public interface RepositoryDenyAllResource extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {
    @Override
    @PermitAll
    long count();

    @Override
    @RolesAllowed("admin")
    List<UserEntity> list(Page page, Sort sort);

    @Override
    @MethodProperties(rolesAllowed = "admin")
    UserEntity get(Long aLong);
}
