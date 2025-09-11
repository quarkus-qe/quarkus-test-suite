package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.Repository;

@Repository(dataStore = "sql-server")
public interface SqlServerFruitRepository extends FruitRepository {
}
