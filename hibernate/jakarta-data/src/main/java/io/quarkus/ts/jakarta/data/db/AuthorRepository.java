package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Repository;

@Repository
public interface AuthorRepository extends BasicRepository<Author, Long> {

}
