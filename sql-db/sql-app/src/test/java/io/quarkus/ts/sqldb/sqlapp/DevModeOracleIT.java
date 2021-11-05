package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
public class DevModeOracleIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("oracle.properties");

    @Test
    public void postgresqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: gvenzl/oracle-xe");
    }
}
