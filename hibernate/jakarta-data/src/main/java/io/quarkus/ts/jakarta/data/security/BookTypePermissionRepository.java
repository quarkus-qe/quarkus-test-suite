package io.quarkus.ts.jakarta.data.security;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
@PermissionsAllowed("read")
public interface BookTypePermissionRepository extends CrudRepository<MyBook, Long> {
    @Query("where title = ?1")
    MyBook findWithTypeLevelPermission(String title);
}
