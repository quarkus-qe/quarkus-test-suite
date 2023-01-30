package io.quarkus.ts.spring.data.rest.secured;

import javax.annotation.security.DenyAll;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import io.quarkus.ts.spring.data.rest.Library;

@RepositoryRestResource(path = "/secured/public")
public interface PublicRepository extends PagingAndSortingRepository<Library, Long> {
    @Override
    @RestResource
    @DenyAll
    Page<Library> findAll(Pageable pageable);
}
