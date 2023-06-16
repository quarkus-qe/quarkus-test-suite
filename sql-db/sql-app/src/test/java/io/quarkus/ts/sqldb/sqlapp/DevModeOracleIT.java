package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@Tag("podman-incompatible") //todo https://github.com/quarkusio/quarkus/issues/33985
public class DevModeOracleIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("oracle.properties");

    @Test
    public void oracleContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/gvenzl/oracle-free");
    }
}
