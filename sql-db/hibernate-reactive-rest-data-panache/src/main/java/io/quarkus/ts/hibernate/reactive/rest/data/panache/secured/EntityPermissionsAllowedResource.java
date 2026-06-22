package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import io.quarkus.hibernate.reactive.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/permissions-allowed")
@PermissionsAllowed("read")
public interface EntityPermissionsAllowedResource extends PanacheEntityResource<ApplicationEntity, Long> {
}
