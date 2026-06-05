package io.quarkus.ts.jakarta.data.security;

import jakarta.annotation.security.RolesAllowed;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
public interface BookInheritedMethodSecuredRepository extends BookParentMethodRepository {
    @Override
    @RolesAllowed("writer")
    @Query("where title = ?1")
    MyBook findWithOverriddenMethodSecurity(String title);
}
