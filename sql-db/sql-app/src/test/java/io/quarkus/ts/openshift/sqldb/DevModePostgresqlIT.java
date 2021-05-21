package io.quarkus.ts.openshift.sqldb;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;

@QuarkusScenario
@DisabledOnNativeImage
public class DevModePostgresqlIT extends AbstractSqlDatabaseIT {

    @DevModeQuarkusApplication
    static RestService app = new RestService().withProperties("postgresql.properties");

    @Override
    protected RestService getApp() {
        return app;
    }
}