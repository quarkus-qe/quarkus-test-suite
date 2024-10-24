package io.quarkus.ts.vertx.sql.handlers;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@Tag("fips-incompatible") // MSSQL works with BC JSSE FIPS which is not native-compatible, we test FIPS elsewhere
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "MSSQL image is not available for IBM s390x and ppc64le")
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
