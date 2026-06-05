package io.quarkus.ts.jakarta.data.security;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
public interface BookRepository extends CrudRepository<MyBook, Long> {
    @Query("where title = ?1")
    MyBook findByTitle(String title);
}
