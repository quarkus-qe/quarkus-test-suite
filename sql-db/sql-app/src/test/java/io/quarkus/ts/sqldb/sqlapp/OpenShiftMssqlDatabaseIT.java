package io.quarkus.ts.sqldb.sqlapp;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.bootstrap.SqlServerService;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@OpenShiftScenario
public class OpenShiftMssqlDatabaseIT extends AbstractSqlDatabaseIT {

    static final int MSSQL_PORT = 1433;

    @Container(image = "${mssql.image}", port = MSSQL_PORT, expectedLog = "Service Broker manager has started")
    static SqlServerService mssql = new SqlServerService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("mssql.properties")
            .withProperty("quarkus.datasource.username", mssql.getUser())
            .withProperty("quarkus.datasource.password", mssql.getPassword())
            .withProperty("quarkus.datasource.jdbc.url", mssql::getJdbcUrl);
}
