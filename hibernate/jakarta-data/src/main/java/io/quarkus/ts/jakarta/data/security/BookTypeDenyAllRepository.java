package io.quarkus.ts.jakarta.data.security;

import jakarta.annotation.security.DenyAll;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
@DenyAll
public interface BookTypeDenyAllRepository extends CrudRepository<MyBook, Long> {
    @Query("where title = ?1")
    MyBook findWithTypeLevelDenyAll(String title);
}
