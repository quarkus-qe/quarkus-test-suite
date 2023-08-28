package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-959")
@Tag("QUARKUS-1026")
@QuarkusScenario
public class DevModePostgresqlIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties");

    @Test
    public void postgresqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/postgres");
    }
}
