package io.quarkus.ts.jakarta.data.db;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.data.repository.Query;
import jakarta.persistence.EntityManager;

import io.quarkus.ts.jakarta.data.interceptor.MyInterceptorBinding;

public interface FruitRepository {

    record View(long count, String names, String ids) {
    }

    @Query("SELECT COUNT(f.id), LISTAGG(f.name, ' & ') WITHIN GROUP (ORDER BY f.id DESC), LISTAGG(CAST(f.id AS string), ',') WITHIN GROUP (ORDER BY f.id ASC) FROM Fruit f")
    View getCurrentView();

    record NameLengths(String name, int length) {
    }

    @Query("SELECT f.name, LENGTH(f.name) FROM Fruit f")
    List<NameLengths> getNameLengths();

    @Query("WHERE dayOfWeek <> io.quarkus.ts.jakarta.data.db.DayOfWeek.MONDAY")
    List<Fruit> getFruitsNotFromMonday();

    // this method is here to test DEV mode changes, so we drop the Thursday constant
    // and expect compilation failure even though it is only used in the @Query annotation value
    @Query("WHERE dayOfWeek <> io.quarkus.ts.jakarta.data.db.DayOfWeek.THURSDAY")
    List<Fruit> getFruitsNotFromThursday();

    @RolesAllowed("admin")
    @Query("SELECT f.id FROM Fruit f")
    List<Long> getAllFruitIdsSecurityInterceptor();

    @MyInterceptorBinding
    @Query("SELECT f.id FROM Fruit f")
    List<Long> getAllFruitIdsCustomInterceptor();

    EntityManager entityManager();

    default void cleanUpFruitTable() {
        // this example comes from the specs, not sure if we need to close the connection
        entityManager().createQuery("DELETE FROM Fruit").executeUpdate();
    }
}
