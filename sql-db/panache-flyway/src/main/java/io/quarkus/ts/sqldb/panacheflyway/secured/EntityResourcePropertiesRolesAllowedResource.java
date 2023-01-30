package io.quarkus.ts.sqldb.panacheflyway.secured;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.sqldb.panacheflyway.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/resource-properties-roles-allowed", rolesAllowed = "admin")
public interface EntityResourcePropertiesRolesAllowedResource extends PanacheEntityResource<ApplicationEntity, Long> {
}
