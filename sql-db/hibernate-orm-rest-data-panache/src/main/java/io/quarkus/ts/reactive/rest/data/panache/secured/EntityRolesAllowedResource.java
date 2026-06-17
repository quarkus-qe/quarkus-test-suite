package io.quarkus.ts.reactive.rest.data.panache.secured;

import jakarta.annotation.security.RolesAllowed;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.reactive.rest.data.panache.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/roles-allowed")
@RolesAllowed("admin")
public interface EntityRolesAllowedResource extends PanacheEntityResource<ApplicationEntity, Long> {
}
