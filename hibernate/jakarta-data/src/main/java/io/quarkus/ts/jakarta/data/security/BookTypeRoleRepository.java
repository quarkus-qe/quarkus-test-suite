package io.quarkus.ts.jakarta.data.security;

import jakarta.annotation.security.RolesAllowed;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
@RolesAllowed("reader")
public interface BookTypeRoleRepository extends CrudRepository<MyBook, Long> {
    @Query("where title = ?1")
    MyBook findWithTypeLevelRole(String title);
}
