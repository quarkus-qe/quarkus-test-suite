package io.quarkus.ts.sqldb.panacheflyway.secured;

import javax.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/roles-allowed")
@RolesAllowed("admin")
public interface EntityRolesAllowedResource extends PanacheEntityResource<ApplicationEntity, Long> {
}
