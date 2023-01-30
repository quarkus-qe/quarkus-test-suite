package io.quarkus.ts.spring.data.rest.secured;

import java.util.Optional;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import io.quarkus.ts.spring.data.rest.Library;

@RepositoryRestResource(path = "/secured/deny-all")
@DenyAll
public interface DenyAllRepository extends CrudRepository<Library, Long> {
    @Override
    @RestResource
    @PermitAll
    Iterable<Library> findAll();

    @Override
    @RestResource
    @RolesAllowed("admin")
    Optional<Library> findById(Long aLong);
}
