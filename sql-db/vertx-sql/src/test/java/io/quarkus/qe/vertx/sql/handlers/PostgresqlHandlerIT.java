package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlHandlerIT extends CommonTestCases {
    private static final String POSTGRESQL_DATABASE = "amadeus";

    @Container(image = "${postgresql.10.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgres = new PostgresqlService()
            .with("test", "test", POSTGRESQL_DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.jdbc.url", postgres::getJdbcUrl)
            .withProperty("quarkus.datasource.reactive.url", postgres::getReactiveUrl)
            .withProperty("app.selected.db", "postgresql")
            // Enable Flyway for Postgresql
            .withProperty("quarkus.flyway.migrate-at-start", "true")
            // Disable Flyway for MySQL
            .withProperty("quarkus.flyway.mysql.migrate-at-start", "false")
            // Disable Flyway for DB2
            .withProperty("quarkus.flyway.db2.migrate-at-start", "false");
}
