package io.quarkus.ts.jakarta.data.db;

import jakarta.data.repository.Repository;

@Repository(dataStore = "mysql")
public interface MySQLAuthorRepository extends AuthorRepository {
}
