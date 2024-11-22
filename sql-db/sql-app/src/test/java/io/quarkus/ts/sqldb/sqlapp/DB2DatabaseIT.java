package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.Db2Service;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
@Tag("fips-incompatible") // Reported in https://github.com/IBM/Db2/issues/43
@Tag("podman-incompatible") //TODO: https://github.com/containers/podman/issues/16432
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2020")
public class DB2DatabaseIT extends AbstractSqlDatabaseIT {

    @Container(image = "${db2.image}", port = 50000, expectedLog = "Setup has completed")
    static Db2Service db2 = new Db2Service();

    @QuarkusApplication(properties = "db2_app.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", db2.getUser())
            .withProperty("quarkus.datasource.password", db2.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", db2::getJdbcUrl);
}
