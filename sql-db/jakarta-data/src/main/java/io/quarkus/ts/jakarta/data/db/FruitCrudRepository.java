package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Query;

public interface FruitCrudRepository extends CrudRepository<Fruit, Long> {

    @Query("SELECT COUNT(this)")
    long countFruits();

    @Query("SELECT COUNT(f) > 0 FROM Fruit f WHERE f.dayOfWeek = ?1")
    boolean existsByDayOfWeek(DayOfWeek dayOfWeek);
}
