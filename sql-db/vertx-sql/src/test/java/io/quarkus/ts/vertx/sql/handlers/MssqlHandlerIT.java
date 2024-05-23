package io.quarkus.ts.vertx.sql.handlers;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFipsAndJava17;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@DisabledOnFipsAndJava17(reason = "https://github.com/quarkusio/quarkus/issues/40813")
@QuarkusScenario
public class MssqlHandlerIT extends CommonTestCases {

    @SqlServerContainer
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.mssql.username", database.getUser())
            .withProperty("quarkus.datasource.mssql.password", database.getPassword())
            .withProperty("quarkus.datasource.mssql.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.datasource.mssql.reactive.url", database::getReactiveUrl)
            .withProperty("quarkus.datasource.mssql.jdbc.additional-jdbc-properties.trustServerCertificate", "true")
            .withProperty("app.selected.db", "mssql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mssql.migrate-at-start", "true");
}
