package io.quarkus.ts.spring.data.rest;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineJpaRepository extends JpaRepository<Magazine, Long> {
}
