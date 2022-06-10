package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMssqlIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("mssql.properties");

    @Test
    public void mmsqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: mcr.microsoft.com/mssql/server");
    }
}
