package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("fips-incompatible") // Reported in https://github.com/IBM/Db2/issues/43
@Tag("podman-incompatible") //TODO: https://github.com/containers/podman/issues/16432
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2020")
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class DB2DatabaseIT extends AbstractSqlDatabaseIT {

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static Db2Service db2 = new Db2Service();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("db2_app.properties")
            .withProperty("quarkus.datasource.username", db2.getUser())
            .withProperty("quarkus.datasource.password", db2.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", db2::getJdbcUrl);
}
