package io.quarkus.ts.openshift.sqldb;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class H2SqlDatabaseIT extends AbstractSqlDatabaseIT {

    @QuarkusApplication
    static RestService app = new RestService().withProperties("h2.properties");

    @Override
    protected RestService getApp() {
        return app;
    }
}
