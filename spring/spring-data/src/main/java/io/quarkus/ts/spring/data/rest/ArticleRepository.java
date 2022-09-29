package io.quarkus.ts.spring.data.rest;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(exported = false, path = "articles", collectionResourceRel = "articles")
public interface ArticleRepository extends PagingAndSortingRepository<Article, Long> {

    @Override
    @RestResource(exported = true)
    Page<Article> findAll(Pageable pageable);

    @Override
    @RestResource(path = "id")
    Optional<Article> findById(Long id);

    @Override
    @RestResource()
    Article save(Article article);
}
