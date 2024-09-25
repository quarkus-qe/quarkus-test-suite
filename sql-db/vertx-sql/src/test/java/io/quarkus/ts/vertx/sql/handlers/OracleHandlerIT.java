package io.quarkus.ts.vertx.sql.handlers;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.OracleService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
public class OracleHandlerIT extends CommonTestCases {
    private static final int ORACLE_PORT = 1521;
    private static final String DATABASE = "amadeus";

    @Container(image = "${oracle.image}", port = ORACLE_PORT, expectedLog = "DATABASE IS READY TO USE!")
    static OracleService oracle = new OracleService()
            .with("test", "test", DATABASE);

    @QuarkusApplication
    static final RestService app = new RestService()
            .withProperty("quarkus.datasource.oracle.username", oracle::getUser)
            .withProperty("quarkus.datasource.oracle.password", oracle::getPassword)
            .withProperty("quarkus.datasource.oracle.jdbc.url", oracle::getJdbcUrl)
            .withProperty("quarkus.datasource.oracle.reactive.url", oracle::getReactiveUrl)
            .withProperty("app.selected.db", "oracle")
            // Enable Flyway for Oracle
            .withProperty("quarkus.flyway.oracle.migrate-at-start", "true");

    @Override
    public void wrongBasketFormatCheckout() {
        // FIXME: drop this method when https://github.com/eclipse-vertx/vertx-sql-client/issues/1343 is fixed
    }
}
