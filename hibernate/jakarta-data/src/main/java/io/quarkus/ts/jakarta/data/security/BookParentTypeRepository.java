package io.quarkus.ts.jakarta.data.security;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;

import io.quarkus.security.Authenticated;
import io.quarkus.ts.jakarta.data.db.MyBook;

@Authenticated
public interface BookParentTypeRepository extends CrudRepository<MyBook, Long> {
    @Query("where title = ?1")
    MyBook findWithInheritedTypeSecurity(String title);
}
