package io.quarkus.ts.openshift.sqldb;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.scenarios.annotations.EnabledIfOpenShiftScenarioPropertyIsTrue;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
@EnabledIfOpenShiftScenarioPropertyIsTrue
public class OpenShiftMssqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int MSSQL_PORT = 1433;

    @Container(image = "mcr.microsoft.com/mssql/rhel/server", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static DefaultService mssql = new DefaultService();

    @QuarkusApplication
    static RestService app = new RestService().withProperties("mssql.properties");

    @Override
    protected RestService getApp() {
        return app;
    }
}
