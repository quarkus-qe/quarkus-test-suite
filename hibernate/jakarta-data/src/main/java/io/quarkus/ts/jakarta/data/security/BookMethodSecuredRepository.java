package io.quarkus.ts.jakarta.data.security;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;

import io.quarkus.security.Authenticated;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.ts.jakarta.data.db.MyBook;

@Repository
public interface BookMethodSecuredRepository extends CrudRepository<MyBook, Long> {
    @Insert
    @RolesAllowed("writer")
    void insertWithWriterRole(MyBook book);

    @Insert
    @PermissionsAllowed("write")
    void insertWithPermission(MyBook book);

    @Update
    @RolesAllowed("writer")
    void updateWithWriterRole(MyBook book);

    @Delete
    @PermissionsAllowed("write")
    void deleteWithPermission(MyBook book);

    @Query("where title = ?1")
    @Authenticated
    MyBook findWithAuthenticated(String title);

    @Query("where title = ?1")
    @DenyAll
    MyBook findWithDenyAll(String title);

    @Query("where title = ?1")
    @PermissionsAllowed("read")
    MyBook findWithPermission(String book);

    @Query("where title = ?1")
    @PermissionsAllowed("read")
    @PermissionsAllowed("write")
    MyBook findWithMultiplePermissions(String title);
}
