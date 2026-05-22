package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnFips;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("QUARKUS-959")
@DisabledOnFips(reason = "https://redhat.atlassian.net/browse/QUARKUS-7816")
@QuarkusScenario
public class DevModeMariadbIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("mariadb_app.properties");

    @Test
    public void mariadbContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/library/mariadb");
    }
}
