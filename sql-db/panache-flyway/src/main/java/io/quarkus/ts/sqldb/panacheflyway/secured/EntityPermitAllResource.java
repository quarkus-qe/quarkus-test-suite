package io.quarkus.ts.sqldb.panacheflyway.secured;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/permit-all")
@PermitAll
public interface EntityPermitAllResource extends PanacheEntityResource<ApplicationEntity, Long> {
    @Override
    @DenyAll
    long count();
}
