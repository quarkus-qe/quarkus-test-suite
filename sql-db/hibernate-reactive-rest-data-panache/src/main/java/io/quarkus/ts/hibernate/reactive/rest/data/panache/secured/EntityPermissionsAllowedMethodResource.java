package io.quarkus.ts.hibernate.reactive.rest.data.panache.secured;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.hibernate.reactive.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.hibernate.reactive.rest.data.panache.ApplicationEntity;
import io.smallrye.mutiny.Uni;

@ResourceProperties(path = "/secured/entity/permissions-allowed-method")
public interface EntityPermissionsAllowedMethodResource extends PanacheEntityResource<ApplicationEntity, Long> {

    @Override
    @PermissionsAllowed("delete")
    Uni<Boolean> delete(Long id);

    @Override
    @PermissionsAllowed("count-1")
    @PermissionsAllowed("count-2")
    Uni<Long> count();

    @PermissionsAllowed("write")
    @GET
    @Path("/custom-count")
    default Uni<Long> customCount() {
        return ApplicationEntity.count();
    }
}
