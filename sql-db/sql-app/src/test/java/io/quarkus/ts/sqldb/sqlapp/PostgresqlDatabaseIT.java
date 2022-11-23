package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            //fixme https://github.com/quarkus-qe/quarkus-test-framework/issues/455
            .withProperty("POSTGRES_USER", "user")
            .withProperty("POSTGRES_PASSWORD", "user")
            .withProperty("POSTGRES_DB", "mydb");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
