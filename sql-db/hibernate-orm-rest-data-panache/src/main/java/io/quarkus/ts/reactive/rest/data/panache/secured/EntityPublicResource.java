package io.quarkus.ts.reactive.rest.data.panache.secured;

import jakarta.annotation.security.DenyAll;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.ts.reactive.rest.data.panache.ApplicationEntity;

@ResourceProperties(path = "/secured/entity/public")
public interface EntityPublicResource extends PanacheEntityResource<ApplicationEntity, Long> {
    @Override
    @DenyAll
    long count();
}
