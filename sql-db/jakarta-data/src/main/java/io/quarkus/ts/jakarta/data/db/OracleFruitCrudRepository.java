package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.Repository;

@Repository(dataStore = "oracle")
public interface OracleFruitCrudRepository extends FruitCrudRepository {
}
