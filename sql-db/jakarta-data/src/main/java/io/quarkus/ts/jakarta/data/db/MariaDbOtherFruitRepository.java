package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.Repository;

@Repository(dataStore = "mariadb")
public interface MariaDbOtherFruitRepository extends OtherFruitRepository {

}
