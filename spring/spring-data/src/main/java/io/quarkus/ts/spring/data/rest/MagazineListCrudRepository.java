package io.quarkus.ts.spring.data.rest;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "magazine-list-crud-rest-repository")
public interface MagazineListCrudRepository extends ListCrudRepository<Magazine, Long> {

    Magazine findByName(String name);

}
