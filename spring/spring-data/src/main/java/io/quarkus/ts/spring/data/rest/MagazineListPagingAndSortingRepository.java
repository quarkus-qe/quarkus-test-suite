package io.quarkus.ts.spring.data.rest;

import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "magazine-list-paging-sorting-rest-repository")
public interface MagazineListPagingAndSortingRepository extends ListPagingAndSortingRepository<Magazine, Long> {

}
