package io.quarkus.ts.sqldb.panacheflyway.secured;

import java.util.List;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/deny-all")
@DenyAll
public interface EntityDenyAllResource extends PanacheEntityResource<ApplicationEntity, Long> {
    @Override
    @PermitAll
    long count();

    @Override
    @RolesAllowed("admin")
    List<ApplicationEntity> list(Page page, Sort sort);

    @Override
    @MethodProperties(rolesAllowed = "admin")
    ApplicationEntity get(Long aLong);
}
