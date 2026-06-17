package io.quarkus.ts.reactive.rest.data.panache.secured;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.reactive.rest.data.panache.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/resource-properties-roles-allowed", rolesAllowed = "admin")
public interface EntityResourcePropertiesRolesAllowedResource extends PanacheEntityResource<ApplicationEntity, Long> {
}
