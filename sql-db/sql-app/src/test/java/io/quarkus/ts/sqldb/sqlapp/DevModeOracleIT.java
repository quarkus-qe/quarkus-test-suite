package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkusio/quarkus/issues/43375")
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class DevModeOracleIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("oracle.properties");

    @Test
    public void oracleContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/gvenzl/oracle-free");
    }
}
