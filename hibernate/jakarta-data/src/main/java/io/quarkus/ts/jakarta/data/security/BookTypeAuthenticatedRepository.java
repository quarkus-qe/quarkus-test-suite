package io.quarkus.ts.jakarta.data.security;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import io.quarkus.security.Authenticated;
import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
@Authenticated
public interface BookTypeAuthenticatedRepository extends CrudRepository<MyBook, Long> {
    @Query("where title = ?1")
    MyBook findWithTypeLevelAuthenticated(String title);
}
