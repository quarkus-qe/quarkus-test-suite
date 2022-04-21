package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.PostgresqlService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlHandlerIT extends CommonTestCases {
    private static final String POSTGRESQL_DATABASE = "amadeus";

    @Container(image = "${postgresql.13.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static PostgresqlService postgres = new PostgresqlService()
            .with("test", "test", POSTGRESQL_DATABASE)
            .withProperty("POSTGRES_USER", "test") //fixme https://github.com/quarkus-qe/quarkus-test-framework/issues/455
            .withProperty("POSTGRES_PASSWORD", "test")
            .withProperty("POSTGRES_DB", POSTGRESQL_DATABASE);;

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.username", postgres.getUser())
            .withProperty("quarkus.datasource.password", postgres.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", postgres::getJdbcUrl)
            .withProperty("quarkus.datasource.reactive.url", postgres::getReactiveUrl)
            .withProperty("app.selected.db", "postgresql")
            // Enable Flyway for Postgresql
            .withProperty("quarkus.flyway.migrate-at-start", "true");
}
