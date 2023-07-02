package io.quarkus.ts.spring.data.nameddatasource;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternativeArticleJpaRepository extends JpaRepository<AlternativeArticle, Long> {
}
