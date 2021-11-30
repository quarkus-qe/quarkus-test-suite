package io.quarkus.qe.vertx.sql.handlers;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnQuarkusSnapshot;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledOnQuarkusSnapshot(reason = "https://github.com/quarkusio/quarkus/issues/21826")
public class MssqlHandlerIT extends CommonTestCases {
    private static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.mssql.username", database.getUser())
            .withProperty("quarkus.datasource.mssql.password", database.getPassword())
            .withProperty("quarkus.datasource.mssql.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.datasource.mssql.reactive.url", database::getReactiveUrl)
            .withProperty("app.selected.db", "mssql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mssql.migrate-at-start", "true");
}
