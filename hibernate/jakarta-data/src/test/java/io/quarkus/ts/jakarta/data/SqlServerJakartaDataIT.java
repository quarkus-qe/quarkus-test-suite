package io.quarkus.ts.jakarta.data;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.SqlServerContainer;

@Tag("fips-incompatible") // MSSQL works with BC JSSE FIPS which is not native-compatible, we test FIPS elsewhere
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2017")
@QuarkusScenario
@Disabled("https://github.com/quarkus-qe/quarkus-test-suite/issues/2631")
public class SqlServerJakartaDataIT extends AbstractJakartaDataIT {

    @SqlServerContainer(tlsEnabled = true)
    static final SqlServerService database = new SqlServerService();

    @QuarkusApplication
    static final RestService app = createApp(database, "sql-server")
            .withProperties(() -> database.getTlsProperties("sql-server"));
}
