package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.scenarios.annotations.DisabledOnSemeruJdk;
import io.quarkus.test.services.DevModeQuarkusApplication;

@Tag("fips-incompatible") // TODO: enable when https://github.com/quarkusio/quarkus/issues/40526 gets fixed
@Tag("QUARKUS-959")
@QuarkusScenario
// TODO: enable when Semeru with condy bug fix is shipped, expected to land in Jan 2027
@DisabledOnSemeruJdk(reason = "OpenJ9 condy bug breaks Quarkus 4 app startup, see https://github.com/eclipse-openj9/openj9/issues/25187")
public class DevModeMysqlIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("mysql.properties");

    @Test
    public void mysqlContainerShouldBeStarted() {
        app.logs().assertContains("Creating container for image: docker.io/library/mysql");
    }
}
