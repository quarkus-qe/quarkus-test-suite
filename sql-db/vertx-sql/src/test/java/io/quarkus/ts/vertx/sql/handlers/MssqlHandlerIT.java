package io.quarkus.ts.vertx.sql.handlers;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@QuarkusScenario
public class MssqlHandlerIT extends CommonTestCases {

    @SqlServerContainer(tlsEnabled = true)
    static SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperties(() -> database.getTlsProperties("mssql"))
            .withProperties("mssql.properties")
            .withProperty("quarkus.datasource.mssql.username", database.getUser())
            .withProperty("quarkus.datasource.mssql.password", database.getPassword())
            .withProperty("quarkus.datasource.mssql.jdbc.url", database::getJdbcUrl)
            .withProperty("quarkus.datasource.mssql.reactive.url", database::getReactiveUrl)
            .withProperty("app.selected.db", "mssql")
            // Enable Flyway for MySQL
            .withProperty("quarkus.flyway.mssql.migrate-at-start", "true");
}
