package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.FileUtils;

@Tag("QUARKUS-959")
@Tag("QUARKUS-1026")
@QuarkusScenario
public class DevModePostgresqlIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties");

    // TODO: Workaround to free resources after application is use. It will be fixed in test framework 0.0.11.
    @AfterAll
    public static void deleteServiceFolder() {
        try {
            FileUtils.deletePath(app.getServiceFolder());
        } catch (Exception ignored) {

        }
    }

    @Test
    public void postgresqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: postgres");
    }
}