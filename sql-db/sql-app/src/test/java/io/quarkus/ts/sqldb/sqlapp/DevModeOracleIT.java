package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@Tag("podman-incompatible") //TODO: https://github.com/quarkusio/quarkus/issues/38003
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/43375")
@DisabledIfSystemProperty(named = "ts.ibm-z-p.missing.services.excludes", matches = "true", disabledReason = "Oracle image is not available for IBM s390x and ppc64le")
public class DevModeOracleIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("oracle.properties");

    @Test
    public void oracleContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/gvenzl/oracle-free");
    }
}
