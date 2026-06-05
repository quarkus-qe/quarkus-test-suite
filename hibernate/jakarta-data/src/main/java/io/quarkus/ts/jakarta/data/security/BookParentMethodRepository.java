package io.quarkus.ts.jakarta.data.security;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;

import io.quarkus.security.Authenticated;
import io.quarkus.ts.jakarta.data.db.MyBook;

public interface BookParentMethodRepository extends CrudRepository<MyBook, Long> {
    @Authenticated
    @Query("where title = ?1")
    MyBook findWithInheritedMethodSecurity(String title);

    @Query("where title = ?1")
    MyBook findWithOverriddenMethodSecurity(String title);
}
