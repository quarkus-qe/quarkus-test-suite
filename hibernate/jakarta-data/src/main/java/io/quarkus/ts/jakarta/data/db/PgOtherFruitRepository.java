package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.Repository;

@Repository(dataStore = "pg")
public interface PgOtherFruitRepository extends OtherFruitRepository {

}
