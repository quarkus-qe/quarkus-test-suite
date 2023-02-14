package io.quarkus.ts.spring.data.rest.secured;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import io.quarkus.ts.spring.data.rest.Library;

@RepositoryRestResource(path = "/secured/permit-all")
@PermitAll
public interface PermitAllRepository extends JpaRepository<Library, Long> {
    @Override
    @RestResource
    @DenyAll
    Page<Library> findAll(Pageable pageable);
}
