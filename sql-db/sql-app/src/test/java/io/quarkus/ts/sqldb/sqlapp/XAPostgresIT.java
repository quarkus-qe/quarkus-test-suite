package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class XAPostgresIT extends AbstractSqlDatabaseIT {

    static final int POSTGRESQL_PORT = 5432;

    @Container(image = "${postgresql.also.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService database = new PostgresqlService()
            .withUser("user")
            .withPassword("user")
            .withDatabase("mydb");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", database.getUser())
            .withProperty("quarkus.datasource.password", database.getPassword())
            .withProperty("quarkus.datasource.jdbc.transactions", "xa")
            .withProperty("quarkus.datasource.jdbc.url", database::getJdbcUrl);
}
