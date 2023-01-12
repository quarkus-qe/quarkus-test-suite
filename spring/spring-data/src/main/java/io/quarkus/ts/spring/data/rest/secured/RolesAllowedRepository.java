package io.quarkus.ts.spring.data.rest.secured;

import java.util.Optional;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.quarkus.ts.spring.data.rest.Library;

@RepositoryRestResource(path = "/secured/roles-allowed")
@RolesAllowed("admin")
public interface RolesAllowedRepository extends CrudRepository<Library, Long> {
    @Override
    @PermitAll
    Optional<Library> findById(Long aLong);
}
