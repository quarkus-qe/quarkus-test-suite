package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class XAPostgresIT extends AbstractSqlDatabaseIT {

    static final int POSTGRESQL_PORT = 5432;

    // TODO rename "postgresql" database to "database" once this issue is fixed
    // quarkus-qe/quarkus-test-framework#641
    @Container(image = "${postgresql.latest.image}", port = POSTGRESQL_PORT, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgresql = new PostgresqlService()
            .withUser("user")
            .withPassword("user")
            .withDatabase("mydb");

    @QuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties")
            .withProperty("quarkus.datasource.username", postgresql.getUser())
            .withProperty("quarkus.datasource.password", postgresql.getPassword())
            .withProperty("quarkus.datasource.jdbc.transactions", "xa")
            .withProperty("quarkus.datasource.jdbc.url", postgresql::getJdbcUrl);
}
