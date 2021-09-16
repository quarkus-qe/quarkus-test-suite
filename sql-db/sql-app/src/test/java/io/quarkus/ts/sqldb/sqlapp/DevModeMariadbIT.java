package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.FileUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMariadbIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties");

    @AfterAll
    public static void deleteServiceFolder() {
        try {
            FileUtils.deletePath(app.getServiceFolder());
        } catch (Exception ignored) {

        }
    }

    @Test
    public void mariadbContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: mariadb");
    }
}
