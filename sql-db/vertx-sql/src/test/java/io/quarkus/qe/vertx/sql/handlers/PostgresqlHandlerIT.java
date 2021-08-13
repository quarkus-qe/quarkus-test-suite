package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class PostgresqlHandlerIT extends CommonTestCases {
    private static final String POSTGRESQL_DATABASE = "amadeus";

    @Container(image = "${postgresql.10.image}", port = 5432, expectedLog = "listening on IPv4 address")
    static DefaultService postgres = new DefaultService()
            .withProperty("POSTGRESQL_USER", "test")
            .withProperty("POSTGRESQL_PASSWORD", "test")
            .withProperty("POSTGRESQL_DATABASE", POSTGRESQL_DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> postgres.getHost().replace("http", "jdbc:postgresql") + ":" +
                            postgres.getPort() + "/" + POSTGRESQL_DATABASE)
            .withProperty("quarkus.datasource.reactive.url",
                    () -> postgres.getHost().replace("http", "postgresql") + ":" +
                            postgres.getPort() + "/" + POSTGRESQL_DATABASE)
            .withProperty("app.selected.db", "postgresql")
            // Enable Flyway for Postgresql
            .withProperty("quarkus.flyway.migrate-at-start", "true")
            // Disable Flyway for MySQL
            .withProperty("quarkus.flyway.mysql.migrate-at-start", "false")
            // Disable Flyway for DB2
            .withProperty("quarkus.flyway.db2.migrate-at-start", "false");

    @Override
    public RestService app() {
        return app;
    }
}
