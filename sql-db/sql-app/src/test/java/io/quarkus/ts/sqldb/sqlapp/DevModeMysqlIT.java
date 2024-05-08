package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("fips-incompatible") // TODO: enable when https://github.com/quarkusio/quarkus/issues/40526 gets fixed
@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMysqlIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties");

    @Test
    public void mysqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/mysql");
    }
}
