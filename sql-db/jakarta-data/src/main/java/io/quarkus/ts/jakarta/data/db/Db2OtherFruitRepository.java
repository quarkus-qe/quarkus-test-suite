package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.Repository;

@Repository(dataStore = "db2")
public interface Db2OtherFruitRepository extends OtherFruitRepository {

}
